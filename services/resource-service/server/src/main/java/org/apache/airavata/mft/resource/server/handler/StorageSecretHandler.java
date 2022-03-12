package org.apache.airavata.mft.resource.server.handler;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.storage.stubs.storagesecret.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@GRpcService
public class StorageSecretHandler extends StorageSecretServiceGrpc.StorageSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(StorageSecretHandler.class);

    @Autowired
    private ResourceBackend backend;

    @Override
    public void getStorageSecret(StorageSecretGetRequest request, StreamObserver<StorageSecret> responseObserver) {
        try {
            this.backend.getStorageSecret(request).ifPresentOrElse(resource -> {
                responseObserver.onNext(resource);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No storage secret with id " + request.getId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving storage secret with id {}", request.getId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving storage secret with id " + request.getId())
                    .asRuntimeException());
        }
    }

    @Override
    public void searchStorageSecret(StorageSecretSearchRequest request, StreamObserver<StorageSecretSearchResponse> responseObserver) {
        try {
            this.backend.searchStorageSecret(request).ifPresentOrElse(resource -> {
                StorageSecretSearchResponse response = StorageSecretSearchResponse.newBuilder()
                        .setStorageSecret(resource).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No storage secret with storage id " + request.getStorageId())
                        .asRuntimeException());
            });
        } catch (Exception e) {
            logger.error("Failed in retrieving storage secret with storage id {}", request.getStorageId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in retrieving storage secret with storage id " + request.getStorageId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createStorageSecret(StorageSecretCreateRequest request, StreamObserver<StorageSecret> responseObserver) {
        try {
            responseObserver.onNext(this.backend.createStorageSecret(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in creating the storage secret", e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in creating the storage secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateStorageSecret(StorageSecretUpdateRequest request, StreamObserver<Empty> responseObserver) {
        try {
            this.backend.updateStorageSecret(request);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed in updating the storage secret {}", request.getStorageSecret().getId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in updating the S3 storage with id " + request.getStorageSecret().getId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteStorageSecret(StorageSecretDeleteRequest request, StreamObserver<Empty> responseObserver) {
        try {
            boolean res = this.backend.deleteStorageSecret(request);
            if (res) {
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new Exception("Failed to delete storage secret with id " + request.getId()));
            }
        } catch (Exception e) {
            logger.error("Failed in deleting the storage secret {}", request.getId(), e);

            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Failed in deleting the storage secret with id " + request.getId())
                    .asRuntimeException());
        }
    }
}
