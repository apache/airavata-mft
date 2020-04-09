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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.S3Resource;
import org.apache.airavata.mft.resource.service.S3ResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.S3Secret;
import org.apache.airavata.mft.secret.service.S3SecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;

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
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {

        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        S3Resource s3Resource = resourceClient.getS3Resource(S3ResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        S3Secret s3Secret = secretClient.getS3Secret(S3SecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(s3Resource.getRegion())
                .build();

        ResourceMetadata metadata = new ResourceMetadata();
        ObjectMetadata s3Metadata = s3Client.getObjectMetadata(s3Resource.getBucketName(), s3Resource.getResourcePath());
        metadata.setResourceSize(s3Metadata.getContentLength());
        metadata.setMd5sum(s3Metadata.getETag());
        metadata.setUpdateTime(s3Metadata.getLastModified().getTime());
        metadata.setCreatedTime(s3Metadata.getLastModified().getTime());
        return metadata;
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {

        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        S3Resource s3Resource = resourceClient.getS3Resource(S3ResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        S3Secret s3Secret = secretClient.getS3Secret(S3SecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(s3Resource.getRegion())
                .build();

        return  s3Client.doesObjectExist(s3Resource.getBucketName(), s3Resource.getResourcePath());
    }
}
