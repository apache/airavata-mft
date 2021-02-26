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
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCResponse;
import org.apache.airavata.mft.agent.http.ConnectorParams;
import org.apache.airavata.mft.agent.http.HttpTransferRequest;
import org.apache.airavata.mft.agent.http.HttpTransferRequestsStore;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.ConnectorResolver;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class RPCParser {

    private static final Logger logger = LoggerFactory.getLogger(RPCParser.class);

    @org.springframework.beans.factory.annotation.Value("${resource.service.host}")
    private String resourceServiceHost;

    @org.springframework.beans.factory.annotation.Value("${resource.service.port}")
    private int resourceServicePort;

    @org.springframework.beans.factory.annotation.Value("${secret.service.host}")
    private String secretServiceHost;

    @org.springframework.beans.factory.annotation.Value("${secret.service.port}")
    private int secretServicePort;

    @org.springframework.beans.factory.annotation.Value("${agent.host}")
    private String agentHost;

    @org.springframework.beans.factory.annotation.Value("${agent.http.port}")
    private Integer agentHttpPort;

    @org.springframework.beans.factory.annotation.Value("${agent.https.enabled}")
    private boolean agentHttpsEnabled;

    @Autowired
    private HttpTransferRequestsStore httpTransferRequestsStore;

    public String resolveRPCRequest(SyncRPCRequest request) throws Exception {
        // TODO implement using the reflection
        ObjectMapper mapper = new ObjectMapper();

        switch (request.getMethod()) {
            case "getFileResourceMetadata":
                String resourceId = request.getParameters().get("resourceId");
                String resourceType = request.getParameters().get("resourceType");
                String resourceToken = request.getParameters().get("resourceToken");

                AuthToken.Builder tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                AuthToken mftAuthorizationToken = tokenBuilder.build();

                Optional<MetadataCollector> metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(resourceType);
                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();
                    metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);
                    FileResourceMetadata fileResourceMetadata = metadataCollector
                            .getFileResourceMetadata(mftAuthorizationToken, resourceId, resourceToken);
                    return mapper.writeValueAsString(fileResourceMetadata);
                }
                break;

            case "getChildFileResourceMetadata":
                resourceId = request.getParameters().get("resourceId");
                resourceType = request.getParameters().get("resourceType");
                resourceToken = request.getParameters().get("resourceToken");
                String childPath = request.getParameters().get("childPath");

                tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                mftAuthorizationToken = tokenBuilder.build();

                metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(resourceType);
                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();
                    metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);
                    FileResourceMetadata fileResourceMetadata = metadataCollector
                            .getFileResourceMetadata(mftAuthorizationToken, resourceId, childPath, resourceToken);
                    return mapper.writeValueAsString(fileResourceMetadata);
                }
                break;

            case "getDirectoryResourceMetadata":
                resourceId = request.getParameters().get("resourceId");
                resourceType = request.getParameters().get("resourceType");
                resourceToken = request.getParameters().get("resourceToken");

                tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                mftAuthorizationToken = tokenBuilder.build();

                metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(resourceType);
                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();
                    metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);
                    DirectoryResourceMetadata dirResourceMetadata = metadataCollector
                            .getDirectoryResourceMetadata(mftAuthorizationToken, resourceId, resourceToken);
                    return mapper.writeValueAsString(dirResourceMetadata);
                }
                break;

            case "getChildDirectoryResourceMetadata":
                resourceId = request.getParameters().get("resourceId");
                resourceType = request.getParameters().get("resourceType");
                resourceToken = request.getParameters().get("resourceToken");
                childPath = request.getParameters().get("childPath");

                tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                mftAuthorizationToken = tokenBuilder.build();

                metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(resourceType);
                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();
                    metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);
                    DirectoryResourceMetadata dirResourceMetadata = metadataCollector
                            .getDirectoryResourceMetadata(mftAuthorizationToken, resourceId, childPath, resourceToken);
                    return mapper.writeValueAsString(dirResourceMetadata);
                }
                break;

            case "submitHttpDownload":
                String storeId = request.getParameters().get("storeId");
                String sourcePath = request.getParameters().get("sourcePath");
                String sourceToken = request.getParameters().get("sourceToken");
                String storeType = request.getParameters().get("storeType");

                tokenBuilder = AuthToken.newBuilder();
                JsonFormat.parser().merge(request.getParameters().get("mftAuthorizationToken"), tokenBuilder);
                mftAuthorizationToken = tokenBuilder.build();

                metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(storeType);
                Optional<Connector> connectorOp = ConnectorResolver.resolveConnector(storeType, "IN");

                if (metadataCollectorOp.isPresent() && connectorOp.isPresent()) {
                    HttpTransferRequest transferRequest = new HttpTransferRequest();
                    transferRequest.setConnectorParams(new ConnectorParams()
                            .setResourceServiceHost(resourceServiceHost)
                            .setResourceServicePort(resourceServicePort)
                            .setSecretServiceHost(secretServiceHost)
                            .setSecretServicePort(secretServicePort)
                            .setStorageId(storeId).setCredentialToken(sourceToken));
                    transferRequest.setTargetResourcePath(sourcePath);
                    transferRequest.setOtherMetadataCollector(metadataCollectorOp.get());
                    transferRequest.setOtherConnector(connectorOp.get());
                    String url = httpTransferRequestsStore.addDownloadRequest(transferRequest);
                    return (agentHttpsEnabled? "https": "http") + "://" + agentHost + ":" + agentHttpPort + "/" + url;
                }
                break;
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
