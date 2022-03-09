package org.apache.airavata.mft.examples.metadata;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.DirectoryMetadataResponse;
import org.apache.airavata.mft.api.service.FetchResourceMetadataRequest;
import org.apache.airavata.mft.api.service.FileMetadataResponse;
import org.apache.airavata.mft.api.service.MFTTransferServiceGrpc;

public class SCPExample {
    public static void main(String args[]) throws Exception {
        MFTTransferServiceGrpc.MFTTransferServiceBlockingStub client = MFTApiClient.MFTApiClientBuilder
                .newBuilder().build().getTransferClient();

        // File metadata
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

        // Directory metadata
        startTime = System.currentTimeMillis();
        DirectoryMetadataResponse directoryMetadataResponse = client.getDirectoryResourceMetadata(FetchResourceMetadataRequest.newBuilder()
                .setResourceId("remote-ssh-dir-resource")
                .setResourceType("SCP")
                .setResourceToken("local-ssh-cred")
                .setTargetAgentId("agent0")
                .build());
        endTime = System.currentTimeMillis();

        System.out.println("Directory metadata response ");
        System.out.println(directoryMetadataResponse);
        System.out.println("Time for processing : " + (endTime - startTime) + " ms");

        // Child file inside parent directoru
        startTime = System.currentTimeMillis();
        fileResourceMetadata = client.getFileResourceMetadata(FetchResourceMetadataRequest.newBuilder()
                .setResourceId("remote-ssh-dir-resource") // Parent directory resource id
                .setResourceType("SCP")
                .setResourceToken("local-ssh-cred")
                .setTargetAgentId("agent0")
                .setChildPath("/tmp/10mb.txt")  // Child file path
                .build());
        endTime = System.currentTimeMillis();
        System.out.println("Child file metadata response ");
        System.out.println(fileResourceMetadata);
        System.out.println("Time for processing : " + (endTime - startTime) + " ms");

        // Child directory inside parent directoru
        startTime = System.currentTimeMillis();
        directoryMetadataResponse = client.getDirectoryResourceMetadata(FetchResourceMetadataRequest.newBuilder()
                .setResourceId("remote-ssh-dir-resource") // Parent directory resource id
                .setResourceType("SCP")
                .setResourceToken("local-ssh-cred")
                .setTargetAgentId("agent0")
                .setChildPath("/tmp/hsperfdata_root") // Child directory path
                .build());
        endTime = System.currentTimeMillis();

        System.out.println("Child directory metadata response ");
        System.out.println(directoryMetadataResponse);
        System.out.println("Time for processing : " + (endTime - startTime) + " ms");

    }
}
