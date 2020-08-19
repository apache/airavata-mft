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
import org.apache.airavata.mft.credential.service.box.BoxSecretServiceGrpc;
import org.apache.airavata.mft.credential.stubs.box.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class BoxServiceHandler extends BoxSecretServiceGrpc.BoxSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(BoxServiceHandler.class);

    @Autowired
    private SecretBackend backend;

    @Override
    public void getBoxSecret(BoxSecretGetRequest request, StreamObserver<BoxSecret> responseObserver) {
        try {
            this.backend.getBoxSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Box Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving Box Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving Box Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
        super.getBoxSecret(request, responseObserver);
    }

    @Override
    public void createBoxSecret(BoxSecretCreateRequest request, StreamObserver<BoxSecret> responseObserver) {
        try {
            this.backend.createBoxSecret(request);
        } catch (Exception e) {
            logger.error("Error in creating Box Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating Box Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateBoxSecret(BoxSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateBoxSecret(request);
        } catch (Exception e) {
            logger.error("Error in updating Box Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating Box Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteBoxSecret(BoxSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.deleteBoxSecret(request);
        } catch (Exception e) {
            logger.error("Error in deleting Box Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting Box Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
