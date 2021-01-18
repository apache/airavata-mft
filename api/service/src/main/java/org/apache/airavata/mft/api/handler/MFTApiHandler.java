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

package org.apache.airavata.mft.api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.SyncRPCClient;
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCResponse;
import org.apache.airavata.mft.api.service.*;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GRpcService
public class MFTApiHandler extends MFTApiServiceGrpc.MFTApiServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(MFTApiHandler.class);

    @Autowired
    private MFTConsulClient mftConsulClient;

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private SyncRPCClient agentRPCClient;

    @org.springframework.beans.factory.annotation.Value("${resource.service.host}")
    private String resourceServiceHost;

    @org.springframework.beans.factory.annotation.Value("${resource.service.port}")
    private int resourceServicePort;

    @org.springframework.beans.factory.annotation.Value("${secret.service.host}")
    private String secretServiceHost;

    @org.springframework.beans.factory.annotation.Value("${secret.service.port}")
    private int secretServicePort;

    @Override
    public void submitTransfer(TransferApiRequest request, StreamObserver<TransferApiResponse> responseObserver) {
        try {
            TransferRequest transferRequest = dozerBeanMapper.map(request, TransferRequest.class);
            Optional.ofNullable(request.getTargetAgentsMap()).ifPresent(transferRequest::setTargetAgents); // Custom mapping

            String transferId = mftConsulClient.submitTransfer(transferRequest);
            logger.info("Submitted the transfer request {}", transferId);

            mftConsulClient.saveTransferState(transferId, new TransferState()
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setState("RECEIVED").setPercentage(0)
                    .setPublisher("api")
                    .setDescription("Received transfer job " + transferId));

            responseObserver.onNext(TransferApiResponse.newBuilder().setTransferId(transferId).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in submitting transfer request", e);
            responseObserver.onError(new Exception("Failed to submit transfer", e));
        }
    }

    @Override
    public void submitHttpUpload(HttpUploadApiRequest request, StreamObserver<HttpUploadApiResponse> responseObserver) {
        super.submitHttpUpload(request, responseObserver);
    }

    @Override
    public void submitHttpDownload(HttpDownloadApiRequest request, StreamObserver<HttpDownloadApiResponse> responseObserver) {
        try {

            // TODO : Automatically derive agent if the target agent is empty
            SyncRPCRequest.SyncRPCRequestBuilder requestBuilder = SyncRPCRequest.SyncRPCRequestBuilder.builder()
                    .withAgentId(request.getTargetAgent())
                    .withMessageId(UUID.randomUUID().toString())
                    .withMethod("submitHttpDownload")
                    .withParameter("storeId", request.getSourceStoreId())
                    .withParameter("sourcePath", request.getSourcePath())
                    .withParameter("sourceToken", request.getSourceToken())
                    .withParameter("storeType", request.getSourceType())
                    .withParameter("mftAuthorizationToken", request.getMftAuthorizationToken());

            SyncRPCResponse rpcResponse = agentRPCClient.sendSyncRequest(requestBuilder.build());

            switch (rpcResponse.getResponseStatus()) {
                case SUCCESS:
                    String url = rpcResponse.getResponseAsStr();
                    HttpDownloadApiResponse downloadResponse = HttpDownloadApiResponse.newBuilder()
                            .setUrl(url)
                            .setTargetAgent(request.getTargetAgent()).build();
                    responseObserver.onNext(downloadResponse);
                    responseObserver.onCompleted();
                    return;
                case FAIL:
                    logger.error("Errored while processing the download request to resource {} in store {}. Error msg : {}",
                            request.getSourcePath(), request.getSourceStoreId(), rpcResponse.getErrorAsStr());
                    responseObserver.onError(new Exception("Errored while processing the the fetch file metadata response. Error msg : " +
                            rpcResponse.getErrorAsStr()));
            }

        } catch (Exception e) {
            logger.error("Error while submitting http download request to path {} in store {}",
                                                request.getSourcePath(), request.getSourceStoreId() , e);
            responseObserver.onError(new Exception("Failed to submit http download request", e));
        }
    }

    @Override
    public void getTransferStates(TransferStateApiRequest request, StreamObserver<TransferStateApiResponse> responseObserver) {
        try {
            List<TransferState> states = mftConsulClient.getTransferStates(request.getTransferId());
            states.forEach(st -> {
                TransferStateApiResponse s = dozerBeanMapper.map(st, TransferStateApiResponse.newBuilder().getClass()).build();
                responseObserver.onNext(s);
            });
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in fetching transfer states", e);
            responseObserver.onError(new Exception("Failed to retrieve transfer states", e));
        }
    }

    @Override
    public void getTransferState(TransferStateApiRequest request, StreamObserver<TransferStateApiResponse> responseObserver) {
        try {
            Optional<TransferState> stateOp = mftConsulClient.getTransferState(request.getTransferId());

            if (stateOp.isPresent()) {
                TransferStateApiResponse s = dozerBeanMapper.map(stateOp.get(),
                    TransferStateApiResponse.newBuilder().getClass()).build();
                responseObserver.onNext(s);
            } else {
                responseObserver.onNext(TransferStateApiResponse.getDefaultInstance());
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in fetching transfer state", e);
            responseObserver.onError(new Exception("Failed to retrieve transfer state", e));
        }
    }

    @Override
    public void getResourceAvailability(ResourceAvailabilityRequest request, StreamObserver<ResourceAvailabilityResponse> responseObserver) {
        try {
            Optional<MetadataCollector> metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(request.getResourceType());
            MetadataCollector metadataCollector = metadataCollectorOp.orElseThrow(
                    () -> new Exception("Could not find a metadata collector for resource " + request.getResourceId()));

            metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);

            Boolean available = metadataCollector.isAvailable(request.getResourceId(), request.getResourceToken());
            responseObserver.onNext(ResourceAvailabilityResponse.newBuilder().setAvailable(available).build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("Error while checking availability of resource " + request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed to check the availability", e));
        }
    }

    /**
     * Fetches metadata for a specified file resource.  This has 2 modes
     * 1. Fetch the metadata of the exact file pointed in the resourceId. This assumes resourceId is an id of a file resource
     * 2. Fetch the metadata of a child directory in the parent directory. To do this, childPath should be provided explicitly
     * and resourceId is the parent resource id. Here resource id should be an id of directory resource
     */
    @Override
    public void getFileResourceMetadata(FetchResourceMetadataRequest request, StreamObserver<FileMetadataResponse> responseObserver) {

        try {
            SyncRPCRequest.SyncRPCRequestBuilder requestBuilder = SyncRPCRequest.SyncRPCRequestBuilder.builder()
                    .withAgentId(request.getTargetAgentId())
                    .withMessageId(UUID.randomUUID().toString())
                    .withParameter("resourceId", request.getResourceId())
                    .withParameter("resourceType", request.getResourceType())
                    .withParameter("resourceToken", request.getResourceToken())
                    .withParameter("mftAuthorizationToken", request.getMftAuthorizationToken());

            if (request.getChildPath().isEmpty()) {
                requestBuilder.withMethod("getFileResourceMetadata");
            } else {
                // If a childPath is specified, look for child directories in the given parent resource id
                requestBuilder.withMethod("getChildFileResourceMetadata");
                requestBuilder.withParameter("childPath", request.getChildPath());
            }

            SyncRPCResponse rpcResponse = agentRPCClient.sendSyncRequest(requestBuilder.build());

            switch (rpcResponse.getResponseStatus()) {
                case SUCCESS:
                    FileResourceMetadata fileResourceMetadata = jsonMapper.readValue(rpcResponse.getResponseAsStr(), FileResourceMetadata.class);
                    FileMetadataResponse.Builder responseBuilder = FileMetadataResponse.newBuilder();
                    dozerBeanMapper.map(fileResourceMetadata, responseBuilder);
                    responseObserver.onNext(responseBuilder.build());
                    responseObserver.onCompleted();
                    return;
                case FAIL:
                    logger.error("Errored while processing the fetch file metadata response for resource id {}. Error msg : {}",
                                                            request.getResourceId(), rpcResponse.getErrorAsStr());
                    responseObserver.onError(new Exception("Errored while processing the the fetch file metadata response. Error msg : " +
                                                            rpcResponse.getErrorAsStr()));
            }
        } catch (Exception e) {
            logger.error("Error while fetching resource metadata for file resource " + request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed to fetch file resource metadata", e));
        }
    }

    /**
     * Fetches metadata for a specified directory resource. This method assumes that the resourceId is an id of
     * a directory resource. This has 2 modes
     * 1. Fetch the metadata of the exact directory pointed in the resourceId
     * 2. Fetch the metadata of a child directory in the parent directory. To do this, childPath should be provided explicitly
     * and resourceId is the parent resource id.
     */
    @Override
    public void getDirectoryResourceMetadata(FetchResourceMetadataRequest request, StreamObserver<DirectoryMetadataResponse> responseObserver) {
        try {
            SyncRPCRequest.SyncRPCRequestBuilder requestBuilder = SyncRPCRequest.SyncRPCRequestBuilder.builder()
                    .withAgentId(request.getTargetAgentId())
                    .withMessageId(UUID.randomUUID().toString())
                    .withParameter("resourceId", request.getResourceId())
                    .withParameter("resourceType", request.getResourceType())
                    .withParameter("resourceToken", request.getResourceToken())
                    .withParameter("mftAuthorizationToken", request.getMftAuthorizationToken());

            if (request.getChildPath().isEmpty()) {
                requestBuilder.withMethod("getDirectoryResourceMetadata");
            } else {
                // If a childPath is specified, look for child directories in the given parent resource id
                requestBuilder.withMethod("getChildDirectoryResourceMetadata");
                requestBuilder.withParameter("childPath", request.getChildPath());
            }

            SyncRPCResponse rpcResponse = agentRPCClient.sendSyncRequest(requestBuilder.build());

            switch (rpcResponse.getResponseStatus()) {
                case SUCCESS:
                    DirectoryResourceMetadata dirResourceMetadata = jsonMapper.readValue(rpcResponse.getResponseAsStr(), DirectoryResourceMetadata.class);
                    DirectoryMetadataResponse.Builder responseBuilder = DirectoryMetadataResponse.newBuilder();
                    dozerBeanMapper.map(dirResourceMetadata, responseBuilder);

                    // As dozer mapper can't map collections in protobuf, do it manually for directories and files
                    for (DirectoryResourceMetadata dm : dirResourceMetadata.getDirectories()) {
                        DirectoryMetadataResponse.Builder db = DirectoryMetadataResponse.newBuilder();
                        dozerBeanMapper.map(dm, db);
                        responseBuilder.addDirectories(db);
                    }

                    for (FileResourceMetadata fm : dirResourceMetadata.getFiles()) {
                        FileMetadataResponse.Builder fb = FileMetadataResponse.newBuilder();
                        dozerBeanMapper.map(fm, fb);
                        responseBuilder.addFiles(fb);
                    }

                    responseObserver.onNext(responseBuilder.build());
                    responseObserver.onCompleted();
                    return;
                case FAIL:
                    logger.error("Errored while processing the fetch directory metadata response for resource id {}. Error msg : {}",
                            request.getResourceId(), rpcResponse.getErrorAsStr());
                    responseObserver.onError(new Exception("Errored while processing the the fetch directory metadata response. Error msg : " +
                            rpcResponse.getErrorAsStr()));
            }
        } catch (Exception e) {
            logger.error("Error while fetching directory resource metadata for resource " + request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed to fetch directory resource metadata", e));
        }
    }
}
