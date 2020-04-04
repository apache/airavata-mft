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

import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.api.service.*;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.dozer.DozerBeanMapper;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@GRpcService
public class MFTApiHandler extends MFTApiServiceGrpc.MFTApiServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(MFTApiHandler.class);

    @Autowired
    private MFTConsulClient mftConsulClient;

    @Autowired
    private DozerBeanMapper dozerBeanMapper;

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
}
