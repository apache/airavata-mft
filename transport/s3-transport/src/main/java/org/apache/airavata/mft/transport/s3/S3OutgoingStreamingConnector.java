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

import edu.colorado.cires.cmg.s3out.AwsS3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.MultipartUploadRequest;
import edu.colorado.cires.cmg.s3out.S3ClientMultipartUpload;
import edu.colorado.cires.cmg.s3out.S3OutputStream;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.OutgoingStreamingConnector;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.GenericResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.OutputStream;
import java.net.URI;

/** NOTE: This implementation uses 3rd party buffering of output stream
 * https://github.com/CI-CMG/aws-s3-outputstream until Amazon SDK supports
 * https://github.com/aws/aws-sdk-java-v2/issues/3128 **/

public class S3OutgoingStreamingConnector implements OutgoingStreamingConnector {

    private static final Logger logger = LoggerFactory.getLogger(S3OutgoingStreamingConnector.class);

    private S3OutputStream s3OutputStream;
    private S3ClientMultipartUpload s3;
    private String resourcePath;
    private S3Storage s3Storage;

    @Override
    public void init(ConnectorConfig cc) throws Exception {

        try (StorageServiceClient storageServiceClient = StorageServiceClientBuilder
                .buildClient(cc.getResourceServiceHost(), cc.getResourceServicePort())) {

            s3Storage = storageServiceClient.s3()
                    .getS3Storage(S3StorageGetRequest.newBuilder().setStorageId(cc.getStorageId()).build());
        }

        this.resourcePath = cc.getResourcePath();

        S3Secret s3Secret;

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                cc.getSecretServiceHost(), cc.getSecretServicePort())) {

            s3Secret = secretClient.s3().getS3Secret(S3SecretGetRequest.newBuilder()
                    .setAuthzToken(cc.getAuthToken())
                    .setSecretId(cc.getCredentialToken()).build());

            AwsCredentials awsCreds;
            if (s3Secret.getSessionToken() == null || s3Secret.getSessionToken().equals("")) {
                awsCreds = AwsBasicCredentials.create(s3Secret.getAccessKey(), s3Secret.getSecretKey());
            } else {
                awsCreds = AwsSessionCredentials.create(s3Secret.getAccessKey(),
                        s3Secret.getSecretKey(),
                        s3Secret.getSessionToken());
            }

            S3Client s3Client = S3Client.builder()
                    .region(Region.of(s3Storage.getRegion())).endpointOverride(new URI(s3Storage.getEndpoint()))
                    .credentialsProvider(() -> awsCreds)
                    .build();

            this.s3 = AwsS3ClientMultipartUpload.builder().s3(s3Client).build();

        }
    }

    @Override
    public void complete() throws Exception {
        if (this.s3OutputStream != null) {
            this.s3OutputStream.done();
            this.s3OutputStream.close();
        }
    }

    @Override
    public void failed() throws Exception {

    }

    @Override
    public OutputStream fetchOutputStream() throws Exception {
        this.s3OutputStream = S3OutputStream.builder()
                .s3(s3)
                .uploadRequest(MultipartUploadRequest.builder()
                        .bucket(s3Storage.getBucketName())
                        .key(resourcePath).build())
                .autoComplete(false)
                .build();

        logger.info("Initialized multipart upload for file {} in bucket {}",
                resourcePath, s3Storage.getBucketName());
        return this.s3OutputStream;
    }
}
