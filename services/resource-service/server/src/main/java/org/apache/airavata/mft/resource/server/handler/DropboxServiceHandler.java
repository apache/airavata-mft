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
import org.apache.airavata.mft.resource.service.dropbox.DropboxServiceGrpc;
import org.apache.airavata.mft.resource.stubs.dropbox.resource.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class DropboxServiceHandler extends DropboxServiceGrpc.DropboxServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(DropboxServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getDropboxResource(DropboxResourceGetRequest request, StreamObserver<DropboxResource> responseObserver) {
        try {
            this.backend.getDropboxResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No dropbox Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving dropbox resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving dropbox resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createDropboxResource(DropboxResourceCreateRequest request, StreamObserver<DropboxResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createDropboxResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the dropbox resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the dropbox resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateDropboxResource(DropboxResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateDropboxResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the dropbox resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the dropbox resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteDropboxResource(DropboxResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteDropboxResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete dropbox Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the dropbox resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the dropbox resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }
}
