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

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.service.odata.ODataStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.odata.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("RSODataServiceHandler")
@GRpcService
public class ODataServiceHandler extends ODataStorageServiceGrpc.ODataStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ODataServiceHandler.class);

    @Autowired
    @Qualifier("SQLResourceBackend")
    private ResourceBackend backend;

    @Override
    public void listODataStorage(ODataStorageListRequest request, StreamObserver<ODataStorageListResponse> responseObserver) {
        try {
            ODataStorageListResponse response = this.backend.listODataStorage(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in retrieving OData storage list", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving OData storage list")
                    .asRuntimeException());
        }
    }

    @Override
    public void getODataStorage(ODataStorageGetRequest request, StreamObserver<ODataStorage> responseObserver) {
        try {
            this.backend.getODataStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No OData storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving OData storage with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving OData storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createODataStorage(ODataStorageCreateRequest request, StreamObserver<ODataStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createODataStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the OData storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the OData storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateODataStorage(ODataStorageUpdateRequest request, StreamObserver<ODataStorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateODataStorage(request);
            responseObserver.onNext(ODataStorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the OData storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the OData storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteODataStorage(ODataStorageDeleteRequest request, StreamObserver<ODataStorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteODataStorage(request);
            if (res) {
                responseObserver.onNext(ODataStorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete OData storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the OData storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the OData storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
