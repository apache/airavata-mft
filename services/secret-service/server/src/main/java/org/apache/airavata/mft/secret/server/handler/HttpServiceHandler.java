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
import org.apache.airavata.mft.credential.service.http.HTTPSecretServiceGrpc;
import org.apache.airavata.mft.credential.stubs.http.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("SSHTTPServiceHandler")
@GRpcService
public class HttpServiceHandler extends HTTPSecretServiceGrpc.HTTPSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(HttpServiceHandler.class);

    @Autowired
    @Qualifier("SQLSecretBackend")
    private SecretBackend backend;

    @Override
    public void getHTTPSecret(HTTPSecretGetRequest request, StreamObserver<HTTPSecret> responseObserver) {
        try {
            this.backend.getHttpSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No HTTP Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving HTTP Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving HTTP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createHTTPSecret(HTTPSecretCreateRequest request, StreamObserver<HTTPSecret> responseObserver) {
        try {
            HTTPSecret httpSecret = this.backend.createHttpSecret(request);
            responseObserver.onNext(httpSecret);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in creating HTTP Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating HTTP Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateHTTPSecret(HTTPSecretUpdateRequest request, StreamObserver<HTTPSecretUpdateResponse> responseObserver) {
        try {
            this.backend.updateHttpSecret(request);
            responseObserver.onNext(HTTPSecretUpdateResponse.newBuilder().setSecretId(request.getSecret().getSecretId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in updating HTTP Secret with id {}", request.getSecret().getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating HTTP Secret with id " + request.getSecret().getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteHTTPSecret(HTTPSecretDeleteRequest request, StreamObserver<HTTPSecretDeleteResponse> responseObserver) {
        try {
            this.backend.deleteHttpSecret(request);
            responseObserver.onNext(HTTPSecretDeleteResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in deleting HTTP Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting HTTP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
