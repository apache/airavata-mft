package org.apache.airavata.mft.api.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.mft.controller.service.MFTControllerServiceGrpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MFTControllerClient {
    private static Map<String, MFTControllerServiceGrpc.MFTControllerServiceBlockingStub> stubCache = new ConcurrentHashMap<>();

    public static MFTControllerServiceGrpc.MFTControllerServiceBlockingStub buildClient(String hostName, int port) {
        String key = hostName + ":" + port;
        if (stubCache.containsKey(key)) {
            return stubCache.get(key);
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress(hostName, port).usePlaintext().build();
        MFTControllerServiceGrpc.MFTControllerServiceBlockingStub stub = MFTControllerServiceGrpc.newBlockingStub(channel);
        stubCache.put(key, stub);
        return stub;
    }
}
