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
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class ResourceServiceHandler extends ResourceServiceGrpc.ResourceServiceImplBase {

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getSCPStorage(SCPStorageGetRequest request, StreamObserver<SCPStorage> responseObserver) {
        this.backend.getSCPStorage(request).ifPresentOrElse(storage -> {
            responseObserver.onNext(storage);
            responseObserver.onCompleted();
        }, () -> {
            responseObserver.onError(new Exception("No SCP Storage with id " + request.getStorageId()));
        });
    }

    @Override
    public void createSCPStorage(SCPStorageCreateRequest request, StreamObserver<SCPStorage> responseObserver) {
        responseObserver.onNext(this.backend.createSCPStorage(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateSCPStorage(SCPStorageUpdateRequest request, StreamObserver<Empty> responseObserver) {
        this.backend.updateSCPStorage(request);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSCPStorage(SCPStorageDeleteRequest request, StreamObserver<Empty> responseObserver) {
        boolean res = this.backend.deleteSCPStorage(request);
        if (res) {
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new Exception("Failed to delete SCP Storage with id " + request.getStorageId()));
        }
    }

    @Override
    public void getSCPResource(SCPResourceGetRequest request, StreamObserver<SCPResource> responseObserver) {
        this.backend.getSCPResource(request).ifPresentOrElse(resource -> {
            responseObserver.onNext(resource);
            responseObserver.onCompleted();
        }, () -> {
            responseObserver.onError(new Exception("No SCP Resource with id " + request.getResourceId()));
        });
    }

    @Override
    public void createSCPResource(SCPResourceCreateRequest request, StreamObserver<SCPResource> responseObserver) {
        responseObserver.onNext(this.backend.createSCPResource(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateSCPResource(SCPResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        this.backend.updateSCPResource(request);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSCPResource(SCPResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        boolean res = this.backend.deleteSCPResource(request);
        if (res) {
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new Exception("Failed to delete SCP Resource with id " + request.getResourceId()));
        }
    }

    @Override
    public void getLocalResource(LocalResourceGetRequest request, StreamObserver<LocalResource> responseObserver) {
        this.backend.getLocalResource(request).ifPresentOrElse(resource -> {
            responseObserver.onNext(resource);
            responseObserver.onCompleted();
        }, () -> {
            responseObserver.onError(new Exception("No Local Resource with id " + request.getResourceId()));
        });
    }

    @Override
    public void createLocalResource(LocalResourceCreateRequest request, StreamObserver<LocalResource> responseObserver) {
        responseObserver.onNext(this.backend.createLocalResource(request));
        responseObserver.onCompleted();
    }

    @Override
    public void updateLocalResource(LocalResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        this.backend.updateLocalResource(request);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteLocalResource(LocalResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        boolean res = this.backend.deleteLocalResource(request);
        if (res) {
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new Exception("Failed to delete Local Resource with id " + request.getResourceId()));
        }    }
}
