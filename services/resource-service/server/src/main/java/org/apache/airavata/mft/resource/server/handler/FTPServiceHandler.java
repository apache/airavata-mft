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
import org.apache.airavata.mft.resource.service.ftp.FTPStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.ftp.storage.*;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListResponse;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class FTPServiceHandler extends FTPStorageServiceGrpc.FTPStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(FTPServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void listFTPStorage(FTPStorageListRequest request, StreamObserver<FTPStorageListResponse> responseObserver) {
        try {
            FTPStorageListResponse response = this.backend.listFTPStorage(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in retrieving FTP storage list", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving FTP storage list")
                    .asRuntimeException());
        }
    }

    @Override
    public void getFTPStorage(FTPStorageGetRequest request, StreamObserver<FTPStorage> responseObserver) {
        try {
            this.backend.getFTPStorage(request).ifPresentOrElse(storage -> {
                responseObserver.onNext(storage);
                responseObserver.onCompleted();
            }, () -> responseObserver.onError(Status.INTERNAL
                    .withDescription("No FTP Storage with id " + request.getStorageId())
                    .asRuntimeException()));
        } catch (Exception e) {
            logger.error("Failed in retrieving FTP storage with id " + request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving FTP storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createFTPStorage(FTPStorageCreateRequest request, StreamObserver<FTPStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createFTPStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the FTP storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the FTP storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateFTPStorage(FTPStorageUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateFTPStorage(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the FTP storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the FTP storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteFTPStorage(FTPStorageDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteFTPStorage(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                logger.error("Failed to delete FTP Storage with id " + request.getStorageId());

                responseObserver.onError(Status.INTERNAL
                        .withDescription("Failed to delete FTP Storage with id " + request.getStorageId())
                        .asRuntimeException());
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the FTP storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the FTP storage")
                    .asRuntimeException());
        }
    }
}
