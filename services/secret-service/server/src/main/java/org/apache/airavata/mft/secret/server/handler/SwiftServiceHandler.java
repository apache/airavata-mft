package org.apache.airavata.mft.secret.server.handler;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.credential.service.swift.SwiftSecretServiceGrpc;
import org.apache.airavata.mft.credential.stubs.swift.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("SSSwiftServiceHandler")
@GRpcService
public class SwiftServiceHandler extends SwiftSecretServiceGrpc.SwiftSecretServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(SwiftServiceHandler.class);

    @Autowired
    @Qualifier("SQLSecretBackend")
    private SecretBackend backend;

    @Override
    public void getSwiftSecret(SwiftSecretGetRequest request, StreamObserver<SwiftSecret> responseObserver) {
        try {
            this.backend.getSwiftSecret(request).ifPresentOrElse(secret -> {
                responseObserver.onNext(secret);
                responseObserver.onCompleted();
            }, () -> {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("No Swift Secret with id " + request.getSecretId())
                        .asRuntimeException());
            });
        } catch (Exception e) {

            logger.error("Error in retrieving Swift Secret with id " + request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in retrieving Swift Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void createSwiftSecret(SwiftSecretCreateRequest request, StreamObserver<SwiftSecret> responseObserver) {
        try {
            SwiftSecret swiftSecret = this.backend.createSwiftSecret(request);
            responseObserver.onNext(swiftSecret);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in creating Swift Secret", e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in creating Swift Secret")
                    .asRuntimeException());
        }
    }

    @Override
    public void updateSwiftSecret(SwiftSecretUpdateRequest request, StreamObserver<SwiftSecretUpdateResponse> responseObserver) {
        try {
            this.backend.updateSwiftSecret(request);
            responseObserver.onNext(SwiftSecretUpdateResponse.newBuilder().setSecretId(request.getSecretId()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in updating Swift Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in updating Swift Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteSwiftSecret(SwiftSecretDeleteRequest request, StreamObserver<SwiftSecretDeleteResponse> responseObserver) {
        try {
            this.backend.deleteSwiftSecret(request);
            responseObserver.onNext(SwiftSecretDeleteResponse.newBuilder().setStatus(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error in deleting Swift Secret with id {}", request.getSecretId(), e);
            responseObserver.onError(Status.INTERNAL.withCause(e)
                    .withDescription("Error in deleting Swift Secret with id " + request.getSecretId())
                    .asRuntimeException());
        }
    }
}
