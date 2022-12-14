package org.apache.airavata.mft.transport.s3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.OutgoingChunkedConnector;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S3OutgoingConnector implements OutgoingChunkedConnector {

    private static final Logger logger = LoggerFactory.getLogger(S3OutgoingConnector.class);

    private S3Storage s3Storage;
    private AmazonS3 s3Client;
    private String resourcePath;

    InitiateMultipartUploadResult initResponse;
    List<PartETag> partETags = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void init(ConnectorConfig cc) throws Exception {

        this.resourcePath = cc.getResourcePath();

        s3Storage = cc.getStorage().getS3();

        S3Secret s3Secret = cc.getSecret().getS3();

        AWSCredentials awsCreds;
        if (s3Secret.getSessionToken() == null || s3Secret.getSessionToken().equals("")) {
            awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());
        } else {
            awsCreds = new BasicSessionCredentials(s3Secret.getAccessKey(),
                    s3Secret.getSecretKey(),
                    s3Secret.getSessionToken());
        }

        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        s3Storage.getEndpoint(), s3Storage.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(s3Storage.getBucketName(),
                resourcePath);
        initResponse = s3Client.initiateMultipartUpload(initRequest);
        logger.info("Initialized multipart upload for file {} in bucket {}",
                resourcePath, s3Storage.getBucketName());
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, String uploadFile) throws Exception {
        File file = new File(uploadFile);
        UploadPartRequest uploadRequest = new UploadPartRequest()
                .withBucketName(s3Storage.getBucketName())
                .withKey(resourcePath)
                .withUploadId(initResponse.getUploadId())
                .withPartNumber(chunkId + 1)
                .withFileOffset(0)
                .withFile(file)
                .withPartSize(file.length());

        UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
        this.partETags.add(uploadResult.getPartETag());
        logger.debug("Uploaded S3 chunk to path {} for resource path {}", uploadFile, resourcePath);
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, InputStream inputStream) throws Exception {
        UploadPartRequest uploadRequest = new UploadPartRequest()
                .withBucketName(s3Storage.getBucketName())
                .withKey(resourcePath)
                .withUploadId(initResponse.getUploadId())
                .withPartNumber(chunkId + 1)
                .withFileOffset(0)
                .withInputStream(inputStream)
                .withPartSize(endByte - startByte);

        UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
        inputStream.close();
        this.partETags.add(uploadResult.getPartETag());
        logger.debug("Uploaded S3 chunk {} for resource path {} using stream", chunkId, resourcePath);
    }

    @Override
    public void complete() throws Exception {
        CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(s3Storage.getBucketName(),
                resourcePath, initResponse.getUploadId(), partETags);
        s3Client.completeMultipartUpload(compRequest);
        logger.info("Completing the upload for file {} in bucket {}", resourcePath,
                s3Storage.getBucketName());
    }

    @Override
    public void failed() throws Exception {

    }
}
