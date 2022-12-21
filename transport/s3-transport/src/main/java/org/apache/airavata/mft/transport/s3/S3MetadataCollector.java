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
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;

import java.io.File;
import java.util.List;

public class S3MetadataCollector implements MetadataCollector {

    boolean initialized = false;
    private S3Storage s3Storage;
    private S3Secret s3Secret;

    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.s3Storage = storage.getS3();
        this.s3Secret = secret.getS3();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("S3 Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath) throws Exception {

        checkInitialized();

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

        ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();

        if (s3Storage.getBucketName().isEmpty() && resourcePath.isEmpty()) {
            List<Bucket> buckets = s3Client.listBuckets();
            DirectoryMetadata.Builder parentDir = DirectoryMetadata.newBuilder();
            parentDir.setResourcePath("");
            parentDir.setFriendlyName("");
            buckets.forEach(b -> {
                DirectoryMetadata.Builder bucketDir = DirectoryMetadata.newBuilder();
                bucketDir.setFriendlyName(b.getName());
                bucketDir.setResourcePath(b.getName());
                bucketDir.setCreatedTime(b.getCreationDate().getTime());
                bucketDir.setUpdateTime(b.getCreationDate().getTime());
                parentDir.addDirectories(bucketDir);
            });
            resourceBuilder.setDirectory(parentDir);

            return resourceBuilder.build();
        }


        String key = s3Client.getObject(s3Storage.getBucketName(), resourcePath).getKey();

        if (key.endsWith("/")) { // Folder

            ObjectListing objectListing = s3Client.listObjects(s3Storage.getBucketName(), key);
            List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();
            for (S3ObjectSummary summary: objectSummaries) {
                if (summary.getKey().endsWith("/")) {
                    DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                    subDirBuilder.setCreatedTime(summary.getLastModified().getTime());
                    subDirBuilder.setUpdateTime(summary.getLastModified().getTime());
                    subDirBuilder.setResourcePath(summary.getKey());
                    subDirBuilder.setFriendlyName(new File(summary.getKey()).getName());
                    dirBuilder.addDirectories(subDirBuilder);
                } else {
                    FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                    fileBuilder.setUpdateTime(summary.getLastModified().getTime());
                    fileBuilder.setCreatedTime(summary.getLastModified().getTime());
                    fileBuilder.setResourcePath(summary.getKey());
                    fileBuilder.setFriendlyName(new File(summary.getKey()).getName());
                    fileBuilder.setResourceSize(summary.getSize());
                    dirBuilder.addFiles(fileBuilder);
                }
            }
            resourceBuilder.setDirectory(dirBuilder);
        } else { // File
            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
            ObjectMetadata fileMetadata = s3Client.getObjectMetadata(s3Storage.getBucketName(), resourcePath);
            fileBuilder.setResourceSize(fileMetadata.getContentLength());
            fileBuilder.setResourcePath(resourcePath);
            fileBuilder.setMd5Sum(fileMetadata.getContentMD5());
            fileBuilder.setFriendlyName(new File(resourcePath).getName());
            fileBuilder.setCreatedTime(fileMetadata.getLastModified().getTime());
            fileBuilder.setUpdateTime(fileMetadata.getLastModified().getTime());
            resourceBuilder.setFile(fileBuilder);
        }

        return resourceBuilder.build();
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {

        checkInitialized();
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
