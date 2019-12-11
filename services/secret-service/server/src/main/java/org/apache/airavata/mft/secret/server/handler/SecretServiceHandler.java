package org.apache.airavata.mft.secret.server.handler;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.secret.service.SCPSecret;
import org.apache.airavata.mft.secret.service.SCPSecretRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class SecretServiceHandler extends SecretServiceGrpc.SecretServiceImplBase {
    @Override
    public void getSCPSecret(SCPSecretRequest request, StreamObserver<SCPSecret> responseObserver) {
        SCPSecret.Builder builder = SCPSecret.newBuilder()
                .setPrivateKey("private")
                .setPrivateKey("pubKey")
                .setPassphrase("pass")
                .setSecretId("sec1");
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
