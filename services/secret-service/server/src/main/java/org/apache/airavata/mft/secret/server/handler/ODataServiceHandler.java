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
import org.apache.airavata.mft.credential.service.odata.ODataSecretServiceGrpc;
import org.apache.airavata.mft.credential.stubs.odata.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("SSODataServiceHandler")
@GRpcService
public class ODataServiceHandler extends ODataSecretServiceGrpc.ODataSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ODataServiceHandler.class);

    @Autowired
    @Qualifier("SQLSecretBackend")
    private SecretBackend backend;

    @Override
    public void getODataSecret(ODataSecretGetRequest request, StreamObserver<ODataSecret> responseObserver) {
        try {
            this.backend.getODataSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No OData Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving OData Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving OData Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
        super.getODataSecret(request, responseObserver);
    }

    @Override
    public void createODataSecret(ODataSecretCreateRequest request, StreamObserver<ODataSecret> responseObserver) {
        try {
            ODataSecret odataSecret = this.backend.createODataSecret(request);
            responseObserver.onNext(odataSecret);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in creating OData Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating OData Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateODataSecret(ODataSecretUpdateRequest request, StreamObserver<ODataSecretUpdateResponse> responseObserver) {
        try {
            this.backend.updateODataSecret(request);
            responseObserver.onNext(ODataSecretUpdateResponse.newBuilder().setSecretId(request.getSecretId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in updating OData Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating OData Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteODataSecret(ODataSecretDeleteRequest request, StreamObserver<ODataSecretDeleteResponse> responseObserver) {
        try {
            this.backend.deleteODataSecret(request);
            responseObserver.onNext(ODataSecretDeleteResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in deleting OData Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting OData Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
