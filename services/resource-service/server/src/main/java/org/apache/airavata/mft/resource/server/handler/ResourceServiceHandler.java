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
import org.apache.airavata.mft.resource.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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

                responseObserver.onError(Status.INTERNAL
                        .withDescription("No SCP Storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving storage with id " + request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createSCPStorage(SCPStorageCreateRequest request, StreamObserver<SCPStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createSCPStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the scp storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the scp storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateSCPStorage(SCPStorageUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateSCPStorage(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the scp storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the scp storage")
                    .asRuntimeException());
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

                responseObserver.onError(Status.INTERNAL
                        .withDescription("Failed to delete SCP Storage with id " + request.getStorageId())
                        .asRuntimeException());
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the scp storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the scp storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void getSCPResource(SCPResourceGetRequest request, StreamObserver<SCPResource> responseObserver) {
        try {
            this.backend.getSCPResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {

                responseObserver.onError(Status.INTERNAL
                        .withDescription("No SCP Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createSCPResource(SCPResourceCreateRequest request, StreamObserver<SCPResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createSCPResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the scp resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the scp resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateSCPResource(SCPResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateSCPResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the scp resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the scp resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteSCPResource(SCPResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteSCPResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {

                responseObserver.onError(Status.INTERNAL
                        .withDescription("Failed to delete SCP Resource with id " + request.getResourceId())
                        .asRuntimeException());
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the scp resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the scp resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void getLocalResource(LocalResourceGetRequest request, StreamObserver<LocalResource> responseObserver) {

        try {
            this.backend.getLocalResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Local Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                .withDescription("Failed in retrieving resource with id " + request.getResourceId())
                .asRuntimeException());
        }
    }

    @Override
    public void createLocalResource(LocalResourceCreateRequest request, StreamObserver<LocalResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createLocalResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the local resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the local resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateLocalResource(LocalResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateLocalResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the local resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the local resource with id " + request.getResourceId())
                    .asRuntimeException());
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

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the local resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void getS3Resource(S3ResourceGetRequest request, StreamObserver<S3Resource> responseObserver) {
        try {
            this.backend.getS3Resource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No S3 Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving S3 resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving S3 resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createS3Resource(S3ResourceCreateRequest request, StreamObserver<S3Resource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createS3Resource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the S3 resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the S3 resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateS3Resource(S3ResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateS3Resource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the S3 resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the S3 resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteS3Resource(S3ResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteS3Resource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete S3 Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the S3 resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the S3 resource with id " + request.getResourceId())
                    .asRuntimeException());
        }    }


    @Override
    public void getBoxResource(BoxResourceGetRequest request, StreamObserver<BoxResource> responseObserver) {
        try {
            this.backend.getBoxResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Box Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving Box resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Box resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createBoxResource(BoxResourceCreateRequest request, StreamObserver<BoxResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createBoxResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the Box resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the Box resource")
                    .asRuntimeException());
        }    }

    @Override
    public void updateBoxResource(BoxResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateBoxResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the Box resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the Box resource with id " + request.getResourceId())
                    .asRuntimeException());
        }    }

    @Override
    public void deleteBoxResource(BoxResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteBoxResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Box Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the Box resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the Box resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }
    @Override
    public void getAzureResource(AzureResourceGetRequest request, StreamObserver<AzureResource> responseObserver) {
        try {
            this.backend.getAzureResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Azure Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving Azure resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Azure resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createAzureResource(AzureResourceCreateRequest request, StreamObserver<AzureResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createAzureResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the Azure resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the Azure resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateAzureResource(AzureResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateAzureResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the Azure resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the Azure resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteAzureResource(AzureResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteAzureResource(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete Azure Resource with id " + request.getResourceId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the Azure resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the Azure resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }
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
