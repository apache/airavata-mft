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
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCResponse;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public String resolveRPCRequest(SyncRPCRequest request) throws Exception {
        // TODO implement using the reflection
        ObjectMapper mapper = new ObjectMapper();

        switch (request.getMethod()) {
            case "getFileResourceMetadata":
                String resourceId = request.getParameters().get("resourceId");
                String resourceType = request.getParameters().get("resourceType");
                String resourceToken = request.getParameters().get("resourceToken");
                String mftAuthorizationToken = request.getParameters().get("mftAuthorizationToken");

                Optional<MetadataCollector> metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(resourceType);
                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();
                    metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);
                    FileResourceMetadata fileResourceMetadata = metadataCollector.getFileResourceMetadata(resourceId, resourceToken);
                    return mapper.writeValueAsString(fileResourceMetadata);
                }
                break;
            case "getDirectoryResourceMetadata":
                resourceId = request.getParameters().get("resourceId");
                resourceType = request.getParameters().get("resourceType");
                resourceToken = request.getParameters().get("resourceToken");
                mftAuthorizationToken = request.getParameters().get("mftAuthorizationToken");

                metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(resourceType);
                if (metadataCollectorOp.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOp.get();
                    metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);
                    DirectoryResourceMetadata dirResourceMetadata = metadataCollector.getDirectoryResourceMetadata(resourceId, resourceToken);
                    return mapper.writeValueAsString(dirResourceMetadata);
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
