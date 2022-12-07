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

package org.apache.airavata.mft.agent.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import org.apache.airavata.mft.admin.ControllerRequestBuilder;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCResponse;
import org.apache.airavata.mft.agent.http.AgentHttpDownloadData;
import org.apache.airavata.mft.agent.http.HttpTransferRequestsStore;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.ConnectorResolver;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingChunkedConnector;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageTypeResolveRequest;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageTypeResolveResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class RPCParser {

    private static final Logger logger = LoggerFactory.getLogger(RPCParser.class);

    @org.springframework.beans.factory.annotation.Value("${agent.advertised.url}")
    private String agentAdvertisedUrl;

    @Autowired
    private HttpTransferRequestsStore httpTransferRequestsStore;

    @Autowired
    private StorageServiceClient storageServiceClient;

    @Autowired
    private ControllerRequestBuilder controllerRequestBuilder;

    public String resolveRPCRequest(SyncRPCRequest request) throws Exception {
        // TODO implement using the reflection
        ObjectMapper mapper = new ObjectMapper();
        logger.info("Accepting sync request {} for method {}", request.getRequestId(), request.getMethod());

        switch (request.getMethod()) {
            case "getFileResourceMetadata":
                String resourcePath = request.getParameters().get("resourcePath");
                String storageId = request.getParameters().get("storageId");
                String resourceToken = request.getParameters().get("resourceToken");

                AuthToken.Builder tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                AuthToken mftAuthorizationToken = tokenBuilder.build();

                StorageTypeResolveResponse storageType = storageServiceClient.common()
                        .resolveStorageType(StorageTypeResolveRequest.newBuilder().setStorageId(storageId).build());

                Optional<MetadataCollector> metadataCollectorOp = MetadataCollectorResolver
                        .resolveMetadataCollector(storageType.getStorageType());

                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();

                    Pair<StorageWrapper, SecretWrapper> credentials = controllerRequestBuilder.createCredentials(storageId, resourceToken);
                    metadataCollector.init(credentials.getLeft() , credentials.getRight());
                    FileMetadata fileResourceMetadata = metadataCollector.getResourceMetadata(resourcePath).getFile();
                    return mapper.writeValueAsString(fileResourceMetadata);
                }
                break;


            case "getDirectoryResourceMetadata":
                resourcePath = request.getParameters().get("resourcePath");
                storageId = request.getParameters().get("storageId");
                resourceToken = request.getParameters().get("resourceToken");

                tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                mftAuthorizationToken = tokenBuilder.build();

                storageType = storageServiceClient.common()
                        .resolveStorageType(StorageTypeResolveRequest.newBuilder().setStorageId(storageId).build());

                metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(storageType.getStorageType());
                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();

                    Pair<StorageWrapper, SecretWrapper> credentials = controllerRequestBuilder.createCredentials(storageId, resourceToken);
                    metadataCollector.init(credentials.getLeft() , credentials.getRight());

                    DirectoryMetadata dirResourceMetadata = metadataCollector
                            .getResourceMetadata(resourcePath).getDirectory();
                    return mapper.writeValueAsString(dirResourceMetadata);
                }
                break;

            case "submitHttpDownload":
                resourcePath = request.getParameters().get("resourcePath");
                String sourceStorageId = request.getParameters().get("sourceStorageId");
                String sourceToken = request.getParameters().get("sourceToken");

                tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                mftAuthorizationToken = tokenBuilder.build();

                storageType = storageServiceClient.common()
                        .resolveStorageType(StorageTypeResolveRequest.newBuilder().setStorageId(sourceStorageId).build());

                metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(storageType.getStorageType());
                Optional<IncomingStreamingConnector> connectorStreamingOp = ConnectorResolver.resolveIncomingStreamingConnector(storageType.getStorageType());
                Optional<IncomingChunkedConnector> connectorChunkedOp = ConnectorResolver.resolveIncomingChunkedConnector(storageType.getStorageType());

                if (metadataCollectorOp.isPresent() && (connectorStreamingOp.isPresent() || connectorChunkedOp.isPresent())) {

                    MetadataCollector metadataCollector = metadataCollectorOp.get();

                    Pair<StorageWrapper, SecretWrapper> credentials = controllerRequestBuilder.createCredentials(sourceStorageId, sourceToken);
                    metadataCollector.init(credentials.getLeft() , credentials.getRight());

                    ResourceMetadata resourceMetadata = metadataCollector.getResourceMetadata(resourcePath);

                    AgentHttpDownloadData.AgentHttpDownloadDataBuilder agentHttpDownloadDataBuilder = AgentHttpDownloadData.AgentHttpDownloadDataBuilder.newBuilder()
                            .withConnectorConfig(ConnectorConfig.ConnectorConfigBuilder.newBuilder()
                                    .withStorage(credentials.getLeft())
                                    .withSecret(credentials.getRight())
                                    .withResourcePath(resourcePath)
                                    .withMetadata(resourceMetadata).build());

                    connectorStreamingOp.ifPresent(agentHttpDownloadDataBuilder::withIncomingStreamingConnector);
                    connectorChunkedOp.ifPresent(agentHttpDownloadDataBuilder::withIncomingChunkedConnector);

                    AgentHttpDownloadData downloadData = agentHttpDownloadDataBuilder.build();

                    String url = httpTransferRequestsStore.addDownloadRequest(downloadData);

                    return (agentAdvertisedUrl.endsWith("/")? agentAdvertisedUrl : agentAdvertisedUrl + "/") + url;
                } else {
                    logger.error("Medata collector or connector is not available for store type {}", storageType.getStorageType());
                    throw new Exception("Medata collector or connector is not available for store type " + storageType.getStorageType());
                }
        }
        logger.error("Unknown method type specified {}", request.getMethod());
        throw new Exception("Unknown method " + request.getMethod());
    }

    public SyncRPCResponse processRPCRequest(SyncRPCRequest request) {
        SyncRPCResponse response = new SyncRPCResponse();
        response.setMessageId(request.getMessageId());
        try {
            String respStr = resolveRPCRequest(request);
            response.setResponseAsStr(respStr);
            response.setResponseStatus(SyncRPCResponse.ResponseStatus.SUCCESS);
        } catch (Exception e) {
            logger.error("Errored while processing the rpc request for message {} and method {}",
                    request.getMessageId(), request.getMethod(), e);
            response.setErrorAsStr(e.getMessage());
            response.setResponseStatus(SyncRPCResponse.ResponseStatus.FAIL);
        }
        return response;
    }
}
