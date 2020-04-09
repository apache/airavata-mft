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
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.S3Resource;
import org.apache.airavata.mft.resource.service.S3ResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.S3Secret;
import org.apache.airavata.mft.secret.service.S3SecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3Sender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(S3Sender.class);

    private AmazonS3 s3Client;
    private S3Resource s3Resource;

    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        this.s3Resource = resourceClient.getS3Resource(S3ResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        S3Secret s3Secret = secretClient.getS3Secret(S3SecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Secret.getAccessKey(), s3Secret.getSecretKey());

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(s3Resource.getRegion())
                .build();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {

        logger.info("Starting S3 Sender stream for transfer {}", context.getTransferId());
        logger.info("Content length for transfer {} {}", context.getTransferId(), context.getMetadata().getResourceSize());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(context.getMetadata().getResourceSize());
        s3Client.putObject(this.s3Resource.getBucketName(), this.s3Resource.getResourcePath(), context.getStreamBuffer().getInputStream(), metadata);

        logger.info("Completed S3 Sender stream for transfer {}", context.getTransferId());
    }
}
