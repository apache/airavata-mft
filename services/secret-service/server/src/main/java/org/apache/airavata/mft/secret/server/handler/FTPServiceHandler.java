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
import org.apache.airavata.mft.credential.service.ftp.FTPSecretServiceGrpc;
import org.apache.airavata.mft.credential.stubs.ftp.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("SSFTPServiceHandler")
@GRpcService
public class FTPServiceHandler extends FTPSecretServiceGrpc.FTPSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(FTPServiceHandler.class);

    @Autowired
    private SecretBackend backend;

    @Override
    public void getFTPSecret(FTPSecretGetRequest request, StreamObserver<FTPSecret> responseObserver) {
        try {
            this.backend.getFTPSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> responseObserver.onError(Status.INTERNAL
                    .withDescription("No FTP Secret with id " + request.getSecretId())
                    .asRuntimeException()));

        } catch (Exception e) {
            logger.error("Error in retrieving FTP Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving FTP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createFTPSecret(FTPSecretCreateRequest request, StreamObserver<FTPSecret> responseObserver) {
        try {
            FTPSecret ftpSecret = this.backend.createFTPSecret(request);
            responseObserver.onNext(ftpSecret);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in creating FTP Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating FTP Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateFTPSecret(FTPSecretUpdateRequest request, StreamObserver<FTPSecretUpdateResponse> responseObserver) {
        try {
            this.backend.updateFTPSecret(request);
            responseObserver.onNext(FTPSecretUpdateResponse.newBuilder().setSecretId(request.getSecretId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in updating FTP Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating FTP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteFTPSecret(FTPSecretDeleteRequest request, StreamObserver<FTPSecretDeleteResponse> responseObserver) {
        try {
            this.backend.deleteFTPSecret(request);
            responseObserver.onNext(FTPSecretDeleteResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in deleting FTP Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting FTP Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
