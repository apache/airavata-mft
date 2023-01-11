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
import org.apache.airavata.mft.resource.service.dropbox.DropboxStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("RSDropboxServiceHandler")
@GRpcService
public class DropboxServiceHandler extends DropboxStorageServiceGrpc.DropboxStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(DropboxServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getDropboxStorage(DropboxStorageGetRequest request, StreamObserver<DropboxStorage> responseObserver) {
        try {
            this.backend.getDropboxStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No dropbox storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving dropbox storage with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving dropbox storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createDropboxStorage(DropboxStorageCreateRequest request, StreamObserver<DropboxStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createDropboxStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the dropbox storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the dropbox storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateDropboxStorage(DropboxStorageUpdateRequest request, StreamObserver<DropboxStorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateDropboxStorage(request);
            responseObserver.onNext(DropboxStorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the dropbox storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the dropbox storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteDropboxStorage(DropboxStorageDeleteRequest request, StreamObserver<DropboxStorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteDropboxStorage(request);
            if (res) {
                responseObserver.onNext(DropboxStorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete dropbox storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the dropbox storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the dropbox storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
