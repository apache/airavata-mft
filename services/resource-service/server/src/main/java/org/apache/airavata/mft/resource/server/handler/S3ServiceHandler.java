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
import org.apache.airavata.mft.resource.service.s3.S3StorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.s3.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class S3ServiceHandler extends S3StorageServiceGrpc.S3StorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(S3ServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void listS3Storage(S3StorageListRequest request, StreamObserver<S3StorageListResponse> responseObserver) {
        try {
            S3StorageListResponse response = this.backend.listS3Storage(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in retrieving S3 storage list", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving S3 storage list")
                    .asRuntimeException());
        }
    }

    @Override
    public void getS3Storage(S3StorageGetRequest request, StreamObserver<S3Storage> responseObserver) {
        try {
            this.backend.getS3Storage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No S3 storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving S3 storage with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving S3 storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createS3Storage(S3StorageCreateRequest request, StreamObserver<S3Storage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createS3Storage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the S3 storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the S3 storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateS3Storage(S3StorageUpdateRequest request, StreamObserver<S3StorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateS3Storage(request);
            responseObserver.onNext(S3StorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the S3 storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the S3 storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteS3Storage(S3StorageDeleteRequest request, StreamObserver<S3StorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteS3Storage(request);
            if (res) {
                responseObserver.onNext(S3StorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete S3 storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the S3 storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the S3 storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
