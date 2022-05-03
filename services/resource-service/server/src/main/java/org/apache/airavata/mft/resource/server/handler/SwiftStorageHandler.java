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

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.service.swift.SwiftStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.swift.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class SwiftStorageHandler extends SwiftStorageServiceGrpc.SwiftStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SwiftStorageHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void listSwiftStorage(SwiftStorageListRequest request, StreamObserver<SwiftStorageListResponse> responseObserver) {
        try {
            SwiftStorageListResponse response = this.backend.listSwiftStorage(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in retrieving Swift storage list", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Swift storage list")
                    .asRuntimeException());
        }
    }

    @Override
    public void getSwiftStorage(SwiftStorageGetRequest request, StreamObserver<SwiftStorage> responseObserver) {
        try {
            this.backend.getSwiftStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Swift storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving Swift storage with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Swift storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createSwiftStorage(SwiftStorageCreateRequest request, StreamObserver<SwiftStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createSwiftStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the Swift storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the Swift storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateSwiftStorage(SwiftStorageUpdateRequest request, StreamObserver<SwiftStorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateSwiftStorage(request);
            responseObserver.onNext(SwiftStorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the Swift storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the Swift storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteSwiftStorage(SwiftStorageDeleteRequest request, StreamObserver<SwiftStorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteSwiftStorage(request);
            if (res) {
                responseObserver.onNext(SwiftStorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Swift storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the Swift storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the Swift storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
