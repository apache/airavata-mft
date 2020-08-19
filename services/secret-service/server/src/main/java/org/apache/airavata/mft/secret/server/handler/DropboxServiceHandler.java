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
import org.apache.airavata.mft.credential.service.dropbox.DropboxSecretServiceGrpc;
import org.apache.airavata.mft.credential.stubs.dropbox.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class DropboxServiceHandler extends DropboxSecretServiceGrpc.DropboxSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(DropboxServiceHandler.class);

    @Autowired
    private SecretBackend backend;

    @Override
    public void getDropboxSecret(DropboxSecretGetRequest request, StreamObserver<DropboxSecret> responseObserver) {
        try {
            this.backend.getDropboxSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Dropbox Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });

        } catch (Exception e) {
            logger.error("Error in retrieving Dropbox Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving Dropbox Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createDropboxSecret(DropboxSecretCreateRequest request, StreamObserver<DropboxSecret> responseObserver) {
        try {
            this.backend.createDropboxSecret(request);
        } catch (Exception e) {
            logger.error("Error in creating Dropbox Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating Dropbox Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateDropboxSecret(DropboxSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateDropboxSecret(request);
        } catch (Exception e) {
            logger.error("Error in updating Dropbox Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating Dropbox Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteDropboxSecret(DropboxSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.deleteDropboxSecret(request);
        } catch (Exception e) {
            logger.error("Error in deleting Dropbox Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting Dropbox Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
