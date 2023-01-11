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
import org.apache.airavata.mft.resource.service.local.LocalStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.local.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("RSLocalServiceHandler")
@GRpcService
public class LocalServiceHandler extends LocalStorageServiceGrpc.LocalStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(LocalServiceHandler.class);

    @Autowired
    @Qualifier("SQLResourceBackend")
    private ResourceBackend backend;

    @Override
    public void listLocalStorage(LocalStorageListRequest request, StreamObserver<LocalStorageListResponse> responseObserver) {
        try {
            LocalStorageListResponse response = this.backend.listLocalStorage(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in retrieving Local storage list", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Local storage list")
                    .asRuntimeException());
        }
    }

    @Override
    public void getLocalStorage(LocalStorageGetRequest request, StreamObserver<LocalStorage> responseObserver) {
        try {
            this.backend.getLocalStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Local storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving storage with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createLocalStorage(LocalStorageCreateRequest request, StreamObserver<LocalStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createLocalStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the local storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the local storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateLocalStorage(LocalStorageUpdateRequest request, StreamObserver<LocalStorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateLocalStorage(request);
            responseObserver.onNext(LocalStorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the local storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the local storge with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteLocalStorage(LocalStorageDeleteRequest request, StreamObserver<LocalStorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteLocalStorage(request);
            if (res) {
                responseObserver.onNext(LocalStorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Local storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the local storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the local storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
