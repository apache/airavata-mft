package org.apache.airavata.mft.examples.metadata;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.FetchResourceMetadataRequest;
import org.apache.airavata.mft.api.service.FileMetadataResponse;
import org.apache.airavata.mft.api.service.MFTApiServiceGrpc;

public class SCPExample {
    public static void main(String args[]) throws Exception {
        MFTApiServiceGrpc.MFTApiServiceBlockingStub client = MFTApiClient.buildClient("localhost", 7004);

        long startTime = System.currentTimeMillis();
        FileMetadataResponse fileResourceMetadata = client.getFileResourceMetadata(FetchResourceMetadataRequest.newBuilder()
                .setResourceId("remote-ssh-resource2")
                .setResourceType("SCP")
                .setResourceToken("local-ssh-cred")
                .setTargetAgentId("agent0")
                .build());
        long endTime = System.currentTimeMillis();
        System.out.println("File metadata response ");
        System.out.println(fileResourceMetadata);
        System.out.println("Time for processing : " + (endTime - startTime) + " ms");
    }
}
