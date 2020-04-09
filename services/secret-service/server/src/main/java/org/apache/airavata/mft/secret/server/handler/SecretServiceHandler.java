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

package org.apache.airavata.mft.secret.server.handler;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GRpcService
public class SecretServiceHandler extends SecretServiceGrpc.SecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SecretServiceHandler.class);

    @Autowired
    private SecretBackend backend;

    @Override
    public void getSCPSecret(SCPSecretGetRequest request, StreamObserver<SCPSecret> responseObserver) {
        try {
            this.backend.getSCPSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No SCP Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });
        } catch (Exception e) {

            logger.error("Error in retrieving SCP Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving SCP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createSCPSecret(SCPSecretCreateRequest request, StreamObserver<SCPSecret> responseObserver) {
        responseObserver.onNext(this.backend.createSCPSecret(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateSCPSecret(SCPSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        this.backend.updateSCPSecret(request);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSCPSecret(SCPSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        boolean res = this.backend.deleteSCPSecret(request);
        if (res) {
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to delete SCP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getS3Secret(S3SecretGetRequest request, StreamObserver<S3Secret> responseObserver) {
        try {
            this.backend.getS3Secret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No S3 Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving S3 Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving S3 Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
        super.getS3Secret(request, responseObserver);
    }

    @Override
    public void createS3Secret(S3SecretCreateRequest request, StreamObserver<S3Secret> responseObserver) {
        try {
            this.backend.createS3Secret(request);
        } catch (Exception e) {
            logger.error("Error in creating S3 Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating S3 Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateS3Secret(S3SecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateS3Secret(request);
        } catch (Exception e) {
            logger.error("Error in updating S3 Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating S3 Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteS3Secret(S3SecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.deleteS3Secret(request);
        } catch (Exception e) {
            logger.error("Error in deleting S3 Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting S3 Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
