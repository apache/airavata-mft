/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.transport.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.Md5Utils;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.OutgoingChunkedConnector;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageGetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S3OutgoingConnector implements OutgoingChunkedConnector {

    private static final Logger logger = LoggerFactory.getLogger(S3OutgoingConnector.class);

    private S3Storage s3Storage;
    private AmazonS3 s3Client;
    private String resourcePath;
    private long resourceLength;

    InitiateMultipartUploadResult initResponse;
    List<PartETag> partETags = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void init(ConnectorConfig cc) throws Exception {

        this.resourcePath = cc.getResourcePath();

        s3Storage = cc.getStorage().getS3();

        S3Secret s3Secret = cc.getSecret().getS3();

        s3Client = S3Util.getInstance().leaseS3Client(s3Secret, s3Storage);

        if (cc.getChunkSize() < cc.getMetadata().getFile().getResourceSize()) {
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(s3Storage.getBucketName(),
                    resourcePath);
            initResponse = s3Client.initiateMultipartUpload(initRequest);
            logger.info("Initialized multipart upload for file {} in bucket {}",
                    resourcePath, s3Storage.getBucketName());
        } else {
            logger.info("Using non-multipart upload for file {} in bucket {}", resourcePath, s3Storage.getBucketName());
        }
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, String uploadFile) throws Exception {
        File file = new File(uploadFile);
        if (initResponse != null) {

            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(s3Storage.getBucketName())
                    .withKey(resourcePath)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(chunkId + 1)
                    .withFileOffset(0)
                    //.withMD5Digest(Md5Utils.md5AsBase64(new File(uploadFile)))
//                    .withFile(file)
                    .withInputStream(new BufferedInputStream(new FileInputStream(file), Math.min(16 * 1024 * 1024, (int) ( endByte - startByte))))
                    .withPartSize(file.length());

            UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
            this.partETags.add(uploadResult.getPartETag());
            logger.debug("Uploaded S3 chunk to path {} for resource path {}", uploadFile, resourcePath);
        } else {
            s3Client.putObject(s3Storage.getBucketName(), resourcePath, uploadFile);
        }
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, InputStream inputStream) throws Exception {
        if (initResponse != null) {
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
        } else {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(resourceLength);
            s3Client.putObject(s3Storage.getBucketName(), resourcePath, inputStream, metadata);
        }
    }

    @Override
    public void complete() throws Exception {

        if (initResponse != null) {
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(s3Storage.getBucketName(),
                    resourcePath, initResponse.getUploadId(), partETags);
            s3Client.completeMultipartUpload(compRequest);
        }
        logger.info("Completed the upload for file {} in bucket {}", resourcePath, s3Storage.getBucketName());
    }

    @Override
    public void failed() throws Exception {
        logger.error("S3 failed to upload chunk to bucket {} for resource path {}", s3Storage.getBucketName(), resourcePath);
    }
}
