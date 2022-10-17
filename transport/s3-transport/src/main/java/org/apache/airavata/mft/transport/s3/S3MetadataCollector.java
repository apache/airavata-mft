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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;

public class S3MetadataCollector implements MetadataCollector {

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    boolean initialized = false;

    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) {
        this.resourceServiceHost = resourceServiceHost;
        this.resourceServicePort = resourceServicePort;
        this.secretServiceHost = secretServiceHost;
        this.secretServicePort = secretServicePort;
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("S3 Metadata Collector is not initialized");
        }
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {

        checkInitialized();

        S3Storage s3Storage;
        try (StorageServiceClient storageServiceClient = StorageServiceClientBuilder
                .buildClient(resourceServiceHost, resourceServicePort)) {

            s3Storage = storageServiceClient.s3()
                    .getS3Storage(S3StorageGetRequest.newBuilder().setStorageId(storageId).build());
        }

        S3Secret s3Secret;
        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                secretServiceHost, secretServicePort)) {
            s3Secret = secretClient.s3().getS3Secret(S3SecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        }

        AWSCredentials awsCreds;
        if (s3Secret.getSessionToken() == null || s3Secret.getSessionToken().equals("")) {
            awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());
        } else {
            awsCreds = new BasicSessionCredentials(s3Secret.getAccessKey(),
                    s3Secret.getSecretKey(),
                    s3Secret.getSessionToken());
        }

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        s3Storage.getEndpoint(),
                        s3Storage.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        FileResourceMetadata metadata = new FileResourceMetadata();
        ObjectMetadata s3Metadata = s3Client.getObjectMetadata(s3Storage.getBucketName(), resourcePath);
        metadata.setResourceSize(s3Metadata.getContentLength());
        metadata.setMd5sum(s3Metadata.getETag());
        metadata.setUpdateTime(s3Metadata.getLastModified().getTime());
        metadata.setCreatedTime(s3Metadata.getLastModified().getTime());
        return metadata;
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String resourcePath, String storageId,
                                                                  String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");    }


    @Override
    public Boolean isAvailable(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {

        checkInitialized();

        S3Storage s3Storage;
        try (StorageServiceClient storageServiceClient = StorageServiceClientBuilder
                .buildClient(resourceServiceHost, resourceServicePort)) {

            s3Storage = storageServiceClient.s3()
                    .getS3Storage(S3StorageGetRequest.newBuilder().setStorageId(storageId).build());
        }

        S3Secret s3Secret;
        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                secretServiceHost, secretServicePort)) {
            s3Secret = secretClient.s3().getS3Secret(S3SecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        }
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        s3Storage.getEndpoint(),
                        s3Storage.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(s3Storage.getRegion())
                .build();

        return s3Client.doesObjectExist(s3Storage.getBucketName(), resourcePath);
    }
}
