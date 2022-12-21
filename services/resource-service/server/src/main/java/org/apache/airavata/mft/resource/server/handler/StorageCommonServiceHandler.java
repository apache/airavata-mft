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
import org.apache.airavata.mft.resource.stubs.storage.common.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sound.midi.Track;

@GRpcService
public class StorageCommonServiceHandler extends StorageCommonServiceGrpc.StorageCommonServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StorageCommonServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void resolveStorageType(StorageTypeResolveRequest request, StreamObserver<StorageTypeResolveResponse> responseObserver) {
        try {
            StorageTypeResolveResponse storageTypeResolveResponse = this.backend.resolveStorageType(request);
            responseObserver.onNext(storageTypeResolveResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in retrieving storage type for storage id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving storage type")
                    .asRuntimeException());
        }
    }

    @Override
    public void registerSecretForStorage(SecretForStorage request, StreamObserver<SecretForStorage> responseObserver) {
        try {
            SecretForStorage secretForStorage = this.backend.registerSecretForStorage(request);
            responseObserver.onNext(secretForStorage);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed registering secret for storage id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed registering secret for storage")
                    .asRuntimeException());
        }

    }

    @Override
    public void getSecretForStorage(SecretForStorageGetRequest request, StreamObserver<SecretForStorage> responseObserver) {
        try {
            SecretForStorage secretForStorage = this.backend.getSecretForStorage(request);
            responseObserver.onNext(secretForStorage);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed fetch secret for storage id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed fetching secret for storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteSecretsForStorage(SecretForStorageDeleteRequest request, StreamObserver<SecretForStorageDeleteResponse> responseObserver) {
        try {
            this.backend.deleteSecretForStorage(request);
            SecretForStorageDeleteResponse.Builder builder = SecretForStorageDeleteResponse.newBuilder();
            builder.setStatus(true);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed deleting secret for storage id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed deleting secret for storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void listStorages(StorageListRequest request, StreamObserver<StorageListResponse> responseObserver) {
        try {
            StorageListResponse storageListResponse = this.backend.listStorage(request);
            responseObserver.onNext(storageListResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed listing storages", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed listing storages\"")
                    .asRuntimeException());
        }
    }
}
