package org.apache.airavata.mft.resource.server.handler;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.service.http.HTTPStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.http.storage.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("RSHTTPServiceHandler")
@GRpcService
public class HttpServiceHandler extends HTTPStorageServiceGrpc.HTTPStorageServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(HttpServiceHandler.class);

    @Autowired
    @Qualifier("SQLResourceBackend")
    private ResourceBackend backend;

    @Override
    public void listHTTPStorage(HTTPStorageListRequest request, StreamObserver<HTTPStorageListResponse> responseObserver) {
        try {
            HTTPStorageListResponse response = this.backend.listHttpStorage(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in retrieving HTTP storage list", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving HTTP storage list")
                    .asRuntimeException());
        }
    }

    @Override
    public void getHTTPStorage(HTTPStorageGetRequest request, StreamObserver<HTTPStorage> responseObserver) {
        try {
            this.backend.getHttpStorage(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No HTTP storage with id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving HTTP storage with id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving HTTP storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createHTTPStorage(HTTPStorageCreateRequest request, StreamObserver<HTTPStorage> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createHttpStorage(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the HTTP storage", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the HTTP storage")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateHTTPStorage(HTTPStorageUpdateRequest request, StreamObserver<HTTPStorageUpdateResponse> responseObserver) {
        try {
            this.backend.updateHttpStorage(request);
            responseObserver.onNext(HTTPStorageUpdateResponse.newBuilder().setStorageId(request.getStorageId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the HTTP storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the HTTP storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteHTTPStorage(HTTPStorageDeleteRequest request, StreamObserver<HTTPStorageDeleteResponse> responseObserver) {
        try {
            boolean res = this.backend.deleteHttpStorage(request);
            if (res) {
                responseObserver.onNext(HTTPStorageDeleteResponse.newBuilder().setStatus(true).build());
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete HTTP storage with id " + request.getStorageId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the HTTP storage {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the HTTP storage with id " + request.getStorageId())
                    .asRuntimeException());
        }
    }
}
