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
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class SecretServiceHandler extends SecretServiceGrpc.SecretServiceImplBase {

    @Autowired
    private SecretBackend backend;

    @Override
    public void getSCPSecret(SCPSecretGetRequest request, StreamObserver<SCPSecret> responseObserver) {
        this.backend.getSCPSecret(request).ifPresentOrElse(secret -> {
            responseObserver.onNext(secret);
            responseObserver.onCompleted();
        }, () -> {
            responseObserver.onError(new Exception("No SCP Secret with id " + request.getSecretId()));
        });
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
            responseObserver.onError(new Exception("Failed to delete SCP Secret with id " + request.getSecretId()));
        }
    }
}
