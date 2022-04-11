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
import org.apache.airavata.mft.resource.service.gcs.GCSStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.gcs.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class GCSServiceHandler extends GCSStorageServiceGrpc.GCSStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GCSServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getGCSStorage(GCSStorageGetRequest request, StreamObserver<GCSStorage> responseObserver) {
        try {
            this.backend.getGCSStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No GCS storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving GCS storage with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving GCS storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createGCSStorage(GCSStorageCreateRequest request, StreamObserver<GCSStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createGCSStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the GCS storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the GCS storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateGCSStorage(GCSStorageUpdateRequest request, StreamObserver<GCSStorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateGCSStorage(request);
            responseObserver.onNext(GCSStorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the GCS storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the GCS storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteGCSStorage(GCSStorageDeleteRequest request, StreamObserver<GCSStorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteGCSStorage(request);
            if (res) {
                responseObserver.onNext(GCSStorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete GCS storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the GCS storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the GCS storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
