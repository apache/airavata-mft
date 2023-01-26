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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingChunkedConnector;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class S3IncomingConnector implements IncomingChunkedConnector, IncomingStreamingConnector {

    private static final Logger logger = LoggerFactory.getLogger(S3IncomingConnector.class);

    private S3Storage s3Storage;
    private AmazonS3 s3Client;
    private String resourcePath;

    @Override
    public void init(ConnectorConfig cc) throws Exception {
        s3Storage = cc.getStorage().getS3();
        this.resourcePath = cc.getResourcePath();

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
    }


    @Override
    public InputStream fetchInputStream() throws Exception {
        S3Object s3object = s3Client.getObject(s3Storage.getBucketName(), resourcePath);
        return s3object.getObjectContent();
    }

    @Override
    public void downloadChunk(int chunkId, long startByte, long endByte, String downloadFile) throws Exception {
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(s3Storage.getBucketName(), resourcePath);
        rangeObjectRequest.setRange(startByte, endByte - 1);
        ObjectMetadata objectMetadata = s3Client.getObject(rangeObjectRequest, new File(downloadFile));
        logger.debug("Downloaded S3 chunk to path {} for resource id {}", downloadFile, resourcePath);
    }

    @Override
    public InputStream downloadChunk(int chunkId, long startByte, long endByte) throws Exception {
        GetObjectRequest rangeObjectRequest = new GetObjectRequest(s3Storage.getBucketName(), resourcePath);
        rangeObjectRequest.setRange(startByte, endByte - 1);
        logger.debug("Fetching input stream for chunk {} in resource path {}", chunkId, resourcePath);
        S3Object object = s3Client.getObject(rangeObjectRequest);
        return object.getObjectContent();
    }

    @Override
    public void complete() throws Exception {

    }

    @Override
    public void failed() throws Exception {

    }
}
