package org.apache.airavata.mft.command.line.sub.s3.storage;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretCreateRequest;
import org.apache.airavata.mft.resource.service.s3.S3StorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageCreateRequest;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecret;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecretCreateRequest;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecretServiceGrpc;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "add")
public class S3StorageAddSubCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Storage Name")
    private String remoteName;

    @CommandLine.Option(names = {"-b", "--bucket"}, description = "Bucket Name")
    private String bucket;

    @CommandLine.Option(names = {"-r", "--region"}, description = "Region")
    private String region;

    @CommandLine.Option(names = {"-e", "--endpoint"}, description = "S3 API Endpoint")
    private String endpoint;

    @CommandLine.Option(names = {"-k", "--key"}, description = "Access Key")
    private String accessKey;

    @CommandLine.Option(names = {"-s", "--secret"}, description = "Access Secret")
    private String accessSecret;

    @Override
    public Integer call() throws Exception {

        AuthToken authToken = AuthToken.newBuilder().build();

        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        S3Secret s3Secret = mftApiClient.getSecretServiceClient().s3()
                .createS3Secret(S3SecretCreateRequest.newBuilder()
                        .setAccessKey(accessKey)
                        .setSecretKey(accessSecret)
                        .setAuthzToken(authToken).build());

        S3StorageServiceGrpc.S3StorageServiceBlockingStub s3StorageClient = mftApiClient.getStorageServiceClient().s3();
        StorageSecretServiceGrpc.StorageSecretServiceBlockingStub storageSecretClient = mftApiClient.getStorageServiceClient().storageSecret();

        S3Storage s3Storage = s3StorageClient.createS3Storage(S3StorageCreateRequest.newBuilder()
                .setName(remoteName)
                .setEndpoint(endpoint)
                .setBucketName(bucket)
                .setRegion(region).build());

        StorageSecret storageSecret = storageSecretClient.createStorageSecret(StorageSecretCreateRequest.newBuilder()
                .setStorageId(s3Storage.getStorageId())
                .setSecretId(s3Secret.getSecretId())
                .setType(StorageSecret.StorageType.S3).build());

        System.out.println("Storage Id " + s3Storage.getStorageId());
        return 0;
    }
}
