package org.apache.airavata.mft.transport.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.OutgoingChunkedConnector;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.GenericResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S3OutgoingConnector implements OutgoingChunkedConnector {

    private static final Logger logger = LoggerFactory.getLogger(S3OutgoingConnector.class);

    private GenericResource resource;
    private AmazonS3 s3Client;

    InitiateMultipartUploadResult initResponse;
    List<PartETag> partETags = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void init(ConnectorConfig cc) throws Exception {
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder
                .buildClient(cc.getResourceServiceHost(), cc.getResourceServicePort())) {

            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(cc.getAuthToken())
                    .setResourceId(cc.getResourceId()).build());
        }

        if (resource.getStorageCase() != GenericResource.StorageCase.S3STORAGE) {
            logger.error("Invalid storage type {} specified for resource {}", resource.getStorageCase(), cc.getResourceId());
            throw new Exception("Invalid storage type specified for resource " + cc.getResourceId());
        }

        S3Storage s3Storage = resource.getS3Storage();

        S3Secret s3Secret;

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                cc.getSecretServiceHost(), cc.getSecretServicePort())) {

            s3Secret = secretClient.s3().getS3Secret(S3SecretGetRequest.newBuilder()
                    .setAuthzToken(cc.getAuthToken())
                    .setSecretId(cc.getCredentialToken()).build());

            BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());

            s3Client = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                            s3Storage.getEndpoint(), s3Storage.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .build();
        }

        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(resource.getS3Storage().getBucketName(),
                resource.getFile().getResourcePath());
        initResponse = s3Client.initiateMultipartUpload(initRequest);
        logger.info("Initialized multipart upload for file {} in bucket {}",
                resource.getFile().getResourcePath(), resource.getS3Storage().getBucketName());
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, String uploadFile) throws Exception {
        File file = new File(uploadFile);
        UploadPartRequest uploadRequest = new UploadPartRequest()
                .withBucketName(resource.getS3Storage().getBucketName())
                .withKey(resource.getFile().getResourcePath())
                .withUploadId(initResponse.getUploadId())
                .withPartNumber(chunkId + 1)
                .withFileOffset(0)
                .withFile(file)
                .withPartSize(file.length());

        UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
        this.partETags.add(uploadResult.getPartETag());
        logger.debug("Uploaded S3 chunk to path {} for resource id {}", uploadFile, resource.getResourceId());
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, InputStream inputStream) throws Exception {
        UploadPartRequest uploadRequest = new UploadPartRequest()
                .withBucketName(resource.getS3Storage().getBucketName())
                .withKey(resource.getFile().getResourcePath())
                .withUploadId(initResponse.getUploadId())
                .withPartNumber(chunkId + 1)
                .withFileOffset(0)
                .withInputStream(inputStream)
                .withPartSize(endByte - startByte);

        UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
        inputStream.close();
        this.partETags.add(uploadResult.getPartETag());
        logger.debug("Uploaded S3 chunk {} for resource id {} using stream", chunkId, resource.getResourceId());
    }

    @Override
    public void complete() throws Exception {
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(resource.getS3Storage().getBucketName(),
                resource.getFile().getResourcePath(), initResponse.getUploadId(), partETags);
        s3Client.completeMultipartUpload(compRequest);
        logger.info("Completing the upload for file {} in bucket {}", resource.getFile().getResourcePath(),
                resource.getS3Storage().getBucketName());
    }
}
