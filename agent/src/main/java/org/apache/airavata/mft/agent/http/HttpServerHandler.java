/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.agent.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.*;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    private final HttpTransferRequestsStore transferRequestsStore;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public HttpServerHandler(HttpTransferRequestsStore transferRequestsStore) {
        this.transferRequestsStore = transferRequestsStore;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        try {
            if (!request.decoderResult().isSuccess()) {
                sendError(ctx, BAD_REQUEST);
                return;
            }

            if (request.method() != GET) {
                sendError(ctx, METHOD_NOT_ALLOWED);
                return;
            }

            final String uri = request.uri().substring(request.uri().lastIndexOf("/") + 1);
            logger.info("Received download request through url {}", uri);

            HttpTransferRequest httpTransferRequest = transferRequestsStore.getDownloadRequest(uri);

            if (httpTransferRequest == null) {
                logger.error("Couldn't find transfer request for uri {}", uri);
                sendError(ctx, NOT_FOUND);
                return;
            }

            Connector connector = httpTransferRequest.getOtherConnector();
            MetadataCollector metadataCollector = httpTransferRequest.getOtherMetadataCollector();

            ConnectorParams params = httpTransferRequest.getConnectorParams();

            // TODO Load from HTTP Headers
            AuthToken authToken = httpTransferRequest.getAuthToken();

            connector.init(params.getResourceServiceHost(),
                    params.getResourceServicePort(), params.getSecretServiceHost(), params.getSecretServicePort());

            metadataCollector.init(params.getResourceServiceHost(), params.getResourceServicePort(),
                    params.getSecretServiceHost(), params.getSecretServicePort());

            Boolean available = metadataCollector.isAvailable(authToken,
                    httpTransferRequest.getResourceId(), httpTransferRequest.getCredentialToken());


            if (!available) {
                logger.error("File resource {} is not available", httpTransferRequest.getResourceId());
                sendError(ctx, NOT_FOUND);
                return;
            }

            FileResourceMetadata fileResourceMetadata = metadataCollector.getFileResourceMetadata(authToken,
                    httpTransferRequest.getResourceId(),
                    httpTransferRequest.getChildResourcePath(),
                    httpTransferRequest.getCredentialToken());

            long fileLength = fileResourceMetadata.getResourceSize();

            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            HttpUtil.setContentLength(response, fileLength);
            setContentTypeHeader(response, httpTransferRequest.getResourceId());

            if (HttpUtil.isKeepAlive(request)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

            // Write the initial line and the header.
            ctx.write(response);

            // Write the content.
            ChannelFuture sendFileFuture;
            ChannelFuture lastContentFuture;

            ConnectorContext connectorContext = new ConnectorContext();
            connectorContext.setStreamBuffer(new DoubleStreamingBuffer());
            connectorContext.setTransferId(uri);
            connectorContext.setMetadata(new FileResourceMetadata()); // TODO Resolve

            TransferTask pullTask = new TransferTask(authToken, httpTransferRequest.getResourceId(),
                    httpTransferRequest.getChildResourcePath(), httpTransferRequest.getCredentialToken(),
                    connectorContext, connector);

            // TODO aggregate pullStatusFuture and sendFileFuture for keepalive test
            Future<Integer> pullStatusFuture = executor.submit(pullTask);

            sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(connectorContext.getStreamBuffer().getInputStream())),
                    ctx.newProgressivePromise());

            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;

            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                    if (total < 0) { // total unknown
                        logger.error(future.channel() + " Transfer progress: " + progress);
                    } else {
                        logger.error(future.channel() + " Transfer progress: " + progress + " / " + total);
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) {
                    System.err.println(future.channel() + " Transfer complete.");
                }
            });

            // Decide whether to close the connection or not.
            if (!HttpUtil.isKeepAlive(request)) {
                // Close the connection when the whole content is written out.
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }

        } catch (Exception e) {
            logger.error("Errored while processing HTTP download request", e);
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String newUri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, newUri);

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static void setContentTypeHeader(HttpResponse response, String path) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, path);
    }
}
