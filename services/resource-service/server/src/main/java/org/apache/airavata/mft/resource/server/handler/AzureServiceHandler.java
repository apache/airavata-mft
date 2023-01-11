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

 package org.apache.airavata.mft.resource.server.handler;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.service.azure.AzureStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.azure.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class AzureServiceHandler extends AzureStorageServiceGrpc.AzureStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(AzureServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getAzureStorage(AzureStorageGetRequest request, StreamObserver<AzureStorage> responseObserver) {
        try {
            this.backend.getAzureStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Azure storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving Azure resource with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Azure resource with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createAzureStorage(AzureStorageCreateRequest request, StreamObserver<AzureStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createAzureStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the Azure storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the Azure resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateAzureStorage(AzureStorageUpdateRequest request, StreamObserver<AzureStorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateAzureStorage(request);
            responseObserver.onNext(AzureStorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the Azure storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the Azure storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteAzureStorage(AzureStorageDeleteRequest request, StreamObserver<AzureStorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteAzureStorage(request);
            if (res) {
                responseObserver.onNext(AzureStorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Azure storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the Azure storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the Azure storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
