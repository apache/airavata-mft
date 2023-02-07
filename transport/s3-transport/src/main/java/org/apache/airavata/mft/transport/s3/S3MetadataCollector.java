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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {

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
                        s3Storage.getEndpoint(), s3Storage.getRegion()))
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

        // If directory path or top level bucket path
        if (resourcePath.endsWith("/") || resourcePath.isEmpty()) { // Directory
            ObjectListing objectListing = s3Client.listObjects(s3Storage.getBucketName(), resourcePath);
            resourceBuilder.setDirectory(processDirectory(resourcePath, objectListing));

        } else if (s3Client.doesObjectExist(s3Storage.getBucketName(), resourcePath)){ // File

            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
            ObjectMetadata fileMetadata = s3Client.getObjectMetadata(s3Storage.getBucketName(), resourcePath);
            fileBuilder.setResourceSize(fileMetadata.getContentLength());
            fileBuilder.setResourcePath(resourcePath);
            fileBuilder.setMd5Sum(fileMetadata.getContentMD5() == null ? "" : fileMetadata.getContentMD5() );
            fileBuilder.setFriendlyName(new File(resourcePath).getName());
            fileBuilder.setCreatedTime(fileMetadata.getLastModified().getTime());
            fileBuilder.setUpdateTime(fileMetadata.getLastModified().getTime());
            resourceBuilder.setFile(fileBuilder);
        } else { // Try if user forgot add trailing /
            ObjectListing objectListing = s3Client.listObjects(s3Storage.getBucketName(), resourcePath + "/");
            resourceBuilder.setDirectory(processDirectory(resourcePath + "/", objectListing));
        }

        return resourceBuilder.build();
    }

    private DirectoryMetadata.Builder processDirectory(String resourcePath, ObjectListing objectListing) {

        List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();

        Map<String, DirectoryMetadata.Builder> subDirCache = new HashMap<>();
        Map<String, List<String>> childTree = new HashMap<>();
        childTree.put(resourcePath, new ArrayList<>());

        DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();
        subDirCache.put(resourcePath, dirBuilder);

        for (S3ObjectSummary summary: objectSummaries) {
            buildStructureRecursively(resourcePath, summary.getKey(), summary, subDirCache, childTree);
        }

        registerChildren(resourcePath, subDirCache, childTree);

        return dirBuilder;
    }

    private void registerChildren(String parentPath, Map<String, DirectoryMetadata.Builder> directoryStore,
                                  Map<String, List<String>> childTree) {
        for (String childDir : childTree.get(parentPath)) {
            registerChildren(childDir, directoryStore, childTree);
            directoryStore.get(parentPath).addDirectories(directoryStore.get(childDir));
        }
    }

    private void buildStructureRecursively(String basePath, String filePath, S3ObjectSummary summary,
                                           Map<String, DirectoryMetadata.Builder> directoryStore,
                                           Map<String, List<String>> childTree) {
        String relativePath = filePath.substring(basePath.length());
        if (relativePath.contains("/")) { // A Directory
            String[] pathSections = relativePath.split("/");

            String thisDirKey = basePath + pathSections[0] + "/";

            if (!directoryStore.containsKey(thisDirKey)) {
                DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                subDirBuilder.setCreatedTime(summary.getLastModified().getTime());
                subDirBuilder.setUpdateTime(summary.getLastModified().getTime());
                subDirBuilder.setResourcePath(thisDirKey);
                subDirBuilder.setFriendlyName(pathSections[0]);
                directoryStore.put(thisDirKey, subDirBuilder);
                childTree.get(basePath).add(thisDirKey);
                childTree.put(thisDirKey, new ArrayList<>());
            }

            //directoryStore.get(basePath).addDirectories(subDirBuilder);
            buildStructureRecursively(thisDirKey, filePath, summary, directoryStore, childTree);

        } else { // A File
            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
            fileBuilder.setUpdateTime(summary.getLastModified().getTime());
            fileBuilder.setCreatedTime(summary.getLastModified().getTime());
            fileBuilder.setResourcePath(summary.getKey());
            fileBuilder.setFriendlyName(new File(summary.getKey()).getName());
            fileBuilder.setResourceSize(summary.getSize());
            directoryStore.get(basePath).addFiles(fileBuilder);
        }
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {

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
                        s3Storage.getEndpoint(), s3Storage.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();

        return s3Client.doesObjectExist(s3Storage.getBucketName(), resourcePath);
    }
}
