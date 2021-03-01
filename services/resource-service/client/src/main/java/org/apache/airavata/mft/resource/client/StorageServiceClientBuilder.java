package org.apache.airavata.mft.resource.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class StorageServiceClientBuilder {

    public static StorageServiceClient buildClient(String hostName, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext().build();
        return new StorageServiceClient(channel);
    }

}
