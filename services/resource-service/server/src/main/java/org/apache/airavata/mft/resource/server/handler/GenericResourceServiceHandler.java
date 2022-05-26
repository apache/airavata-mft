package org.apache.airavata.mft.resource.server.handler;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.stubs.common.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class GenericResourceServiceHandler extends GenericResourceServiceGrpc.GenericResourceServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GenericResourceServiceHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getGenericResource(GenericResourceGetRequest request, StreamObserver<GenericResource> responseObserver) {
        try {
            this.backend.getGenericResource(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No GCS Resource with id " + request.getResourceId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving generic resource with id {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving Generic resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createGenericResource(GenericResourceCreateRequest request, StreamObserver<GenericResource> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createGenericResource(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the GCS resource", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the GCS resource")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateGenericResource(GenericResourceUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateGenericResource(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the GCS resource {}", request.getResourceId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the GCS resource with id " + request.getResourceId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteGenericResource(GenericResourceDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteGenericResource(request);
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
