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
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    private final HttpTransferRequestsStore transferRequestsStore;

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

            AgentHttpDownloadData downloadData = transferRequestsStore.getDownloadRequest(uri);

            if (downloadData == null) {
                logger.error("Couldn't find transfer request for uri {}", uri);
                sendError(ctx, NOT_FOUND);
                return;
            }

            long fileLength = downloadData.getConnectorConfig().getMetadata().getFile().getResourceSize();

            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            HttpUtil.setContentLength(response, fileLength);
            setContentTypeHeader(response, downloadData.getConnectorConfig().getMetadata().getFile().getFriendlyName());

            if (HttpUtil.isKeepAlive(request)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            // Write the initial line and the header.
            ctx.write(response);

            // Write the content.
            ChannelFuture sendFileFuture;
            ChannelFuture lastContentFuture;

            // TODO: Support chunked streaming
            if (downloadData.getIncomingStreamingConnector() == null && downloadData.getIncomingChunkedConnector() != null) {
                logger.error("Chunked data download is not yes supported in Http transport");
                throw new Exception("Chunked data download is not yes supported in Http transport");
            }

            IncomingStreamingConnector incomingStreamingConnector = downloadData.getIncomingStreamingConnector();
            incomingStreamingConnector.init(downloadData.getConnectorConfig());
            InputStream inputStream = incomingStreamingConnector.fetchInputStream();

            sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedStream(inputStream)),
                    ctx.newProgressivePromise());

            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;

            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                    if (total < 0) { // total unknown
                        logger.debug(future.channel() + " Transfer progress: " + progress);
                    } else {
                        logger.debug(future.channel() + " Transfer progress: " + progress + " / " + total);
                    }
                }

                @Override
                public void operationComplete(ChannelProgressiveFuture future) {
                    logger.info(future.channel() + " Transfer complete.");
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
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, "attachment; filename=\"" + path+ "\"");
    }
}
