package org.apache.airavata.mft.resource.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;

public class ResourceServiceClient {
    public static ResourceServiceGrpc.ResourceServiceBlockingStub buildClient(String hostName, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext().build();
        return ResourceServiceGrpc.newBlockingStub(channel);
    }
}
