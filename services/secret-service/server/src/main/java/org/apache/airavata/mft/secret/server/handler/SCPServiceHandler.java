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

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.credential.service.scp.SCPSecretServiceGrpc;
import org.apache.airavata.mft.credential.stubs.scp.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("SSSCPServiceHandler")
@GRpcService
public class SCPServiceHandler extends SCPSecretServiceGrpc.SCPSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SCPServiceHandler.class);

    @Autowired
    @Qualifier("SQLSecretBackend")
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
    public void updateSCPSecret(SCPSecretUpdateRequest request, StreamObserver<SCPSecretUpdateResponse> responseObserver) {
        this.backend.updateSCPSecret(request);
        responseObserver.onNext(SCPSecretUpdateResponse.newBuilder().setSecretId(request.getSecretId()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSCPSecret(SCPSecretDeleteRequest request, StreamObserver<SCPSecretDeleteResponse> responseObserver) {
        boolean res = this.backend.deleteSCPSecret(request);
        if (res) {
            responseObserver.onNext(SCPSecretDeleteResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to delete SCP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
