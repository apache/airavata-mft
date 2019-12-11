package org.apache.airavata.mft.secret.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;

public class SecretServiceClient {
    public static SecretServiceGrpc.SecretServiceBlockingStub buildClient(String hostName, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext().build();
        return SecretServiceGrpc.newBlockingStub(channel);
    }
}
