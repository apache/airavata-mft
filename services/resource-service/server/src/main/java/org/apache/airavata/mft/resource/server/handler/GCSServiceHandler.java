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
import org.apache.airavata.mft.resource.service.gcs.GCSResourceServiceGrpc;
import org.apache.airavata.mft.resource.stubs.gcs.resource.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class GCSServiceHandler extends GCSResourceServiceGrpc.GCSResourceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GCSServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getGCSResource(GCSResourceGetRequest request, StreamObserver<GCSResource> responseObserver) {
        try {
            this.backend.getGCSResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No GCS Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving GCS resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving GCS resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createGCSResource(GCSResourceCreateRequest request, StreamObserver<GCSResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createGCSResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the GCS resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the GCS resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateGCSResource(GCSResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateGCSResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the GCS resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the GCS resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteGCSResource(GCSResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteGCSResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete GCS Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the GCS resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the GCS resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }
}
