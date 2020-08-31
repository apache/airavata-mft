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

package org.apache.airavata.mft.controller.handler;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.controller.service.MFTControllerServiceGrpc;
import org.apache.airavata.mft.controller.service.TransferStatusRequest;
import org.apache.airavata.mft.controller.service.TransferStatusResponse;
import org.apache.airavata.mft.controller.sql.entity.TransferRequestEntity;
import org.apache.airavata.mft.controller.sql.repository.TransferRequestRepository;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@GRpcService
public class MFTControllerHandler extends MFTControllerServiceGrpc.MFTControllerServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(MFTControllerHandler.class);

    @Autowired
    private TransferRequestRepository transferRequestRepository;

    @Override
    public void getAllTransferDetails(com.google.protobuf.Empty request, StreamObserver<TransferStatusResponse> responseObserver) {
        try {
            List<TransferRequestEntity> transferRequests = transferRequestRepository.findAll();
            transferRequests.forEach(tr -> responseObserver.onNext(convertRequestEntityToStatusResponse(tr)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in fetching transfer requests", e);
            responseObserver.onError(new Exception("Failed to retrieve transfer requests", e));
        }
    }

    @Override
    public void getTransferDetails(TransferStatusRequest request, StreamObserver<TransferStatusResponse> responseObserver) {
        try {
            Optional<TransferRequestEntity> transferRequestEntity = transferRequestRepository.findByTransferId(request.getTransferId());
            transferRequestEntity.ifPresent(tr -> responseObserver.onNext(convertRequestEntityToStatusResponse(tr)));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in fetching transfer requests", e);
            responseObserver.onError(new Exception("Failed to retrieve transfer requests", e));
        }
    }

    private TransferStatusResponse convertRequestEntityToStatusResponse(TransferRequestEntity transferRequestEntity) {
        return TransferStatusResponse.newBuilder()
                .setTransferId(transferRequestEntity.getTransferId())
                .setSourceId(transferRequestEntity.getSourceId())
                .setSourceType(transferRequestEntity.getSourceType())
                .setSourceToken(transferRequestEntity.getSourceToken())
                .setSourceResourceBackend(transferRequestEntity.getSourceResourceBackend())
                .setSourceCredentialBackend(transferRequestEntity.getSourceCredentialBackend())
                .setDestinationId(transferRequestEntity.getDestinationId())
                .setDestinationType(transferRequestEntity.getDestinationId())
                .setDestinationToken(transferRequestEntity.getDestinationToken())
                .setDestResourceBackend(transferRequestEntity.getDestResourceBackend())
                .setDestCredentialBackend(transferRequestEntity.getDestCredentialBackend())
                .setInitialDate(convertEpochTimeToTimeStamp(transferRequestEntity.getInitialEpochTimeInMillis()))
                .setFinalDate(convertEpochTimeToTimeStamp(transferRequestEntity.getCurrentEpochTimeInMillis()))
                .build();
    }

    /**
     * EPOCH time is the number of seconds passed from Jan 1st 1970.
     * Using google.protobuf.util.Timestamps, this api converts EPOCH time to google.protobuf.Timestamp
     *
     * When a TransferRequest isn't still initiated by an Agent, the currentEpochTime will be null.
     * In that case, the API returns null.
     * @param epochTimeInMillis
     * @return google.protobuf.Timestamp
     */
    private Timestamp convertEpochTimeToTimeStamp(Long epochTimeInMillis) {
        if (epochTimeInMillis == null)
            return null;

        return Timestamps.fromMillis(epochTimeInMillis);
    }
}
