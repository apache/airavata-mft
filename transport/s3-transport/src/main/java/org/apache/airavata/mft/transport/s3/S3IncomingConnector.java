package org.apache.airavata.mft.transport.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingChunkedConnector;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
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

public class S3IncomingConnector implements IncomingChunkedConnector, IncomingStreamingConnector {

    private static final Logger logger = LoggerFactory.getLogger(S3IncomingConnector.class);

    private GenericResource resource;
    private AmazonS3 s3Client;

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
    }


    @Override
    public InputStream fetchInputStream() throws Exception {
        S3Object s3object = s3Client.getObject(resource.getS3Storage().getBucketName(), resource.getFile().getResourcePath());
        return s3object.getObjectContent();
    }

    @Override
    public InputStream fetchInputStream(String childPath) throws Exception {
        S3Object s3object = s3Client.getObject(resource.getS3Storage().getBucketName(), childPath);
        return s3object.getObjectContent();
    }

    @Override
    public void downloadChunk(int chunkId, long startByte, long endByte, String downloadFile) throws Exception {
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(resource.getS3Storage().getBucketName(),
                resource.getFile().getResourcePath());
        rangeObjectRequest.setRange(startByte, endByte - 1);
        ObjectMetadata objectMetadata = s3Client.getObject(rangeObjectRequest, new File(downloadFile));
        logger.info("Downloaded S3 chunk to path {} for resource id {}", downloadFile, resource.getResourceId());
    }

    @Override
    public void complete() throws Exception {

    }
}
