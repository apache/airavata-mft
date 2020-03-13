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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class ResourceServiceHandler extends ResourceServiceGrpc.ResourceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getSCPStorage(SCPStorageGetRequest request, StreamObserver<SCPStorage> responseObserver) {
        try {
            this.backend.getSCPStorage(request).ifPresentOrElse(storage -> {
                responseObserver.onNext(storage);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(new Exception("No SCP Storage with id " + request.getStorageId()));
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving storage with id " + request.getStorageId(), e);
            responseObserver.onError(new Exception("Failed in retrieving storage with id " + request.getStorageId()));
        }
    }

    @Override
    public void createSCPStorage(SCPStorageCreateRequest request, StreamObserver<SCPStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createSCPStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the scp storage", e);
            responseObserver.onError(new Exception("Failed in creating the scp storage", e));
        }
    }

    @Override
    public void updateSCPStorage(SCPStorageUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateSCPStorage(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the scp storage {}", request.getStorageId(), e);
            responseObserver.onError(new Exception("Failed in updating the scp storage", e));
        }

    }

    @Override
    public void deleteSCPStorage(SCPStorageDeleteRequest request, StreamObserver<Empty> responseObserver) {

        try {
            boolean res = this.backend.deleteSCPStorage(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                logger.error("Failed to delete SCP Storage with id " + request.getStorageId());
                responseObserver.onError(new Exception("Failed to delete SCP Storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the scp storage {}", request.getStorageId(), e);
            responseObserver.onError(new Exception("Failed in deleting the scp storage", e));
        }
    }

    @Override
    public void getSCPResource(SCPResourceGetRequest request, StreamObserver<SCPResource> responseObserver) {
        try {
            this.backend.getSCPResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(new Exception("No SCP Resource with id " + request.getResourceId()));
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving resource with id {}", request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed in retrieving resource with id " + request.getResourceId()));
        }
    }

    @Override
    public void createSCPResource(SCPResourceCreateRequest request, StreamObserver<SCPResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createSCPResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the scp resource", e);
            responseObserver.onError(new Exception("Failed in creating the scp resource", e));
        }
    }

    @Override
    public void updateSCPResource(SCPResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateSCPResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the scp resource {}", request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed in updating the scp resource", e));
        }
    }

    @Override
    public void deleteSCPResource(SCPResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteSCPResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete SCP Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the scp resource {}", request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed in deleting the scp resource", e));
        }
    }

    @Override
    public void getLocalResource(LocalResourceGetRequest request, StreamObserver<LocalResource> responseObserver) {

        try {
            this.backend.getLocalResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(new Exception("No Local Resource with id " + request.getResourceId()));
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving resource with id {}", request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed in retrieving resource with id " + request.getResourceId()));
        }
    }

    @Override
    public void createLocalResource(LocalResourceCreateRequest request, StreamObserver<LocalResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createLocalResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the local resource", e);
            responseObserver.onError(new Exception("Failed in creating the local resource", e));
        }
    }

    @Override
    public void updateLocalResource(LocalResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateLocalResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the local resource {}", request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed in updating the local resource", e));
        }
    }

    @Override
    public void deleteLocalResource(LocalResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteLocalResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Local Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the local resource {}", request.getResourceId(), e);
            responseObserver.onError(new Exception("Failed in deleting the local resource", e));
        }
    }
}
