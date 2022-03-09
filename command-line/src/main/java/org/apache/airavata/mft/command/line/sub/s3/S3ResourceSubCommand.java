package org.apache.airavata.mft.command.line.sub.s3;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretCreateRequest;
import org.apache.airavata.mft.resource.service.s3.S3StorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageCreateRequest;
import picocli.CommandLine;

@CommandLine.Command(name = "remote")
public class S3ResourceSubCommand {

    @CommandLine.Command(name = "add")
    void addS3Resource() {

        AuthToken authToken = AuthToken.newBuilder().build();

        String remoteName = System.console().readLine("Remote Name: ");
        String bucket = System.console().readLine("Bucket: ");
        String region = System.console().readLine("Region: ");
        String endpoint = System.console().readLine("S3 Endpoint: ");
        String useTLS = System.console().readLine("Use TLS [Y/n]: ");

        String accessKey = System.console().readLine("Access Key: ");
        String accessSecret = System.console().readLine("Access Secret: ");
        System.out.println("Adding S3 Secret");
        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        S3Secret s3Secret = mftApiClient.getSecretServiceClient().s3()
                .createS3Secret(S3SecretCreateRequest.newBuilder()
                        .setAccessKey(accessKey)
                        .setSecretKey(accessSecret)
                        .setAuthzToken(authToken).build());

        System.out.println("Adding S3 Storage");
        S3StorageServiceGrpc.S3StorageServiceBlockingStub s3StorageClient = mftApiClient.getStorageServiceClient().s3();

        S3Storage s3Storage = s3StorageClient.createS3Storage(S3StorageCreateRequest.newBuilder()
                .setStorageId(remoteName)
                .setEndpoint(endpoint)
                .setBucketName(bucket)
                .setUseTLS("Y".equals(useTLS))
                .setRegion(region).build());

        System.out.println("Successfully created the remote " + remoteName);

    }

    @CommandLine.Command(name = "delete")
    void deleteS3Resource(@CommandLine.Parameters(index = "0") String resourceId) {
        System.out.println("Deleting S3 Resource " + resourceId);
    }

    @CommandLine.Command(name = "list")
    void listS3Resource() {
        System.out.println("Listing S3 Resource");
    }

    @CommandLine.Command(name = "get")
    void getS3Resource(@CommandLine.Parameters(index = "0") String resourceId) {
        System.out.println("Getting S3 Resource " + resourceId);
    }
}
