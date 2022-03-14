package org.apache.airavata.mft.command.line.sub.s3.storage;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.command.line.sub.s3.storage.S3StorageAddSubCommand;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListResponse;
import picocli.CommandLine;

@CommandLine.Command(name = "remote", subcommands = {S3StorageAddSubCommand.class})
public class S3StorageSubCommand {

    @CommandLine.Command(name = "delete")
    void deleteS3Resource(@CommandLine.Parameters(index = "0") String resourceId) {
        System.out.println("Deleting S3 Resource " + resourceId);
    }

    @CommandLine.Command(name = "list")
    void listS3Resource() {
        System.out.println("Listing S3 Resource");
        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        S3StorageListResponse s3StorageListResponse = mftApiClient.getStorageServiceClient().s3()
                .listS3Storage(S3StorageListRequest.newBuilder().setOffset(0).setLimit(10).build());

        s3StorageListResponse.getStoragesList().forEach(s -> {
            System.out.println("Storage Id : " + s.getStorageId() + " Name : " + s.getName()+ " Bucket : " + s.getBucketName());
        });
    }

    @CommandLine.Command(name = "get")
    void getS3Resource(@CommandLine.Parameters(index = "0") String resourceId) {
        System.out.println("Getting S3 Resource " + resourceId);
    }
}
