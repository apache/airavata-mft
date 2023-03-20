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
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.agent.transport.MetadataCollectorResolver;
import org.apache.airavata.mft.agent.transport.TransportClassLoaderCache;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RPCParser {

    private static final Logger logger = LoggerFactory.getLogger(RPCParser.class);

    public String resolveRPCRequest(SyncRPCRequest request, TransportClassLoaderCache transportCache) throws Exception {
        // TODO implement using the reflection
        ObjectMapper mapper = new ObjectMapper();
        logger.info("Accepting sync request {} for method {}", request.getRequestId(), request.getMethod());

        switch (request.getMethod()) {
            case "getResourceMetadata":
                String requestStr = request.getParameters().get("request");

                GetResourceMetadataRequest.Builder directResourceMetadataReq = GetResourceMetadataRequest.newBuilder();
                JsonFormat.parser().merge(requestStr, directResourceMetadataReq);
                GetResourceMetadataRequest req = directResourceMetadataReq.build();

                Optional<MetadataCollector> metadataCollectorOptional = MetadataCollectorResolver
                        .resolveMetadataCollector(req.getStorage().getStorageCase().name(), transportCache);
                if (metadataCollectorOptional.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOptional.get();
                    metadataCollector.init(req.getStorage(), req.getSecret());
                    ResourceMetadata resourceMetadata = metadataCollector
                            .getResourceMetadata(req.getResourcePath(), directResourceMetadataReq.getRecursiveSearch());
                    return JsonFormat.printer().print(resourceMetadata);
                } else {
                    throw new Exception("No metadata collector for type " + req.getStorage().getStorageCase().name());
                }
            case "getResourceAvailability":
               requestStr = request.getParameters().get("request");

                directResourceMetadataReq = GetResourceMetadataRequest.newBuilder();
                req = directResourceMetadataReq.build();
                JsonFormat.parser().merge(requestStr, directResourceMetadataReq);

                metadataCollectorOptional = MetadataCollectorResolver
                        .resolveMetadataCollector(req.getStorage().getStorageCase().name(), transportCache);
                if (metadataCollectorOptional.isPresent()) {
                    MetadataCollector metadataCollector = metadataCollectorOptional.get();
                    metadataCollector.init(req.getStorage(), req.getSecret());
                    Boolean available = metadataCollector.isAvailable(req.getResourcePath());
                    return available.toString();
                } else {
                    throw new Exception("No metadata collector for type " + req.getStorage().getStorageCase().name());
                }

        }
        logger.error("Unknown method type specified {}", request.getMethod());
        throw new Exception("Unknown method " + request.getMethod());
    }

    public SyncRPCResponse processRPCRequest(SyncRPCRequest request, TransportClassLoaderCache transportCache) {
        SyncRPCResponse response = new SyncRPCResponse();
        response.setMessageId(request.getMessageId());
        try {
            String respStr = resolveRPCRequest(request, transportCache);
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
