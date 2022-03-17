package org.apache.airavata.mft.command.line.sub.s3.storage;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.command.line.CommandLineUtil;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListResponse;
import picocli.CommandLine;

import java.util.List;

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

        List<S3Storage> storagesList = s3StorageListResponse.getStoragesList();

        int[] columnWidth = {40, 15, 15, 15, 35};
        String[][] content = new String[storagesList.size() + 1][5];
        String[] headers = {"STORAGE ID", "NAME", "BUCKET", "REGION", "ENDPOINT"};
        content[0] = headers;


        for (int i = 1; i <= storagesList.size(); i ++) {
            S3Storage s3Storage = storagesList.get(i - 1);
            content[i][0] = s3Storage.getStorageId();
            content[i][1] = s3Storage.getName();
            content[i][2] = s3Storage.getBucketName();
            content[i][3] = s3Storage.getRegion();
            content[i][4] = s3Storage.getEndpoint();
        }

        CommandLineUtil.printTable(columnWidth, content);
    }

    @CommandLine.Command(name = "get")
    void getS3Resource(@CommandLine.Parameters(index = "0") String resourceId) {
        System.out.println("Getting S3 Resource " + resourceId);
    }
}
