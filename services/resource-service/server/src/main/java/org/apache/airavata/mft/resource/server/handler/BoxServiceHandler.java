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
import org.apache.airavata.mft.resource.service.box.BoxResourceServiceGrpc;
import org.apache.airavata.mft.resource.stubs.box.resource.*;
import org.apache.airavata.mft.resource.stubs.box.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class BoxServiceHandler extends BoxResourceServiceGrpc.BoxResourceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BoxServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getBoxResource(BoxResourceGetRequest request, StreamObserver<BoxResource> responseObserver) {
        try {
            this.backend.getBoxResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Box Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving Box resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Box resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createBoxResource(BoxResourceCreateRequest request, StreamObserver<BoxResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createBoxResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the Box resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the Box resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateBoxResource(BoxResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateBoxResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the Box resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the Box resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteBoxResource(BoxResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteBoxResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Box Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the Box resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the Box resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getBoxStorage(BoxStorageGetRequest request, StreamObserver<BoxStorage> responseObserver) {
        try {
            this.backend.getBoxStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Box storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving Box resource with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Box storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createBoxStorage(BoxStorageCreateRequest request, StreamObserver<BoxStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createBoxStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the Box storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the Box storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateBoxStorage(BoxStorageUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateBoxStorage(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the Box storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the Box storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteBoxStorage(BoxStorageDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteBoxStorage(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Box storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the Box storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the Box storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
