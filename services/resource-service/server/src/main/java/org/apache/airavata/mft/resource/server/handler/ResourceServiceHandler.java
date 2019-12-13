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
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.service.*;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ResourceServiceHandler extends ResourceServiceGrpc.ResourceServiceImplBase {

    @Override
    public void getSCPStorage(SCPStorageGetRequest request, StreamObserver<SCPStorage> responseObserver) {
        super.getSCPStorage(request, responseObserver);
    }

    @Override
    public void createSCPStorage(SCPStorageCreateRequest request, StreamObserver<SCPStorage> responseObserver) {
        super.createSCPStorage(request, responseObserver);
    }

    @Override
    public void updateSCPStorage(SCPStorageUpdateRequest request, StreamObserver<Empty> responseObserver) {
        super.updateSCPStorage(request, responseObserver);
    }

    @Override
    public void deleteSCPStorage(SCPStorageDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deleteSCPStorage(request, responseObserver);
    }

    @Override
    public void getSCPResource(SCPResourceGetRequest request, StreamObserver<SCPResource> responseObserver) {
        SCPResource.Builder resourceBuilder = SCPResource.newBuilder().setResourceId("001")
                .setScpStorage(SCPStorage.newBuilder()
                        .setHost("localhost")
                        .setPort(22).build());
        responseObserver.onNext(resourceBuilder.build());
        responseObserver.onCompleted();


    }

    @Override
    public void createSCPResource(SCPResourceCreateRequest request, StreamObserver<SCPResource> responseObserver) {
        super.createSCPResource(request, responseObserver);
    }

    @Override
    public void updateSCPResource(SCPResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        super.updateSCPResource(request, responseObserver);
    }

    @Override
    public void deleteSCPResource(SCPResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deleteSCPResource(request, responseObserver);
    }

    @Override
    public void getLocalResource(LocalResourceGetRequest request, StreamObserver<LocalResource> responseObserver) {
        super.getLocalResource(request, responseObserver);
    }

    @Override
    public void createLocalResource(LocalResourceCreateRequest request, StreamObserver<LocalResource> responseObserver) {
        super.createLocalResource(request, responseObserver);
    }

    @Override
    public void updateLocalResource(LocalResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        super.updateLocalResource(request, responseObserver);
    }

    @Override
    public void deleteLocalResource(LocalResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        super.deleteLocalResource(request, responseObserver);
    }
}
