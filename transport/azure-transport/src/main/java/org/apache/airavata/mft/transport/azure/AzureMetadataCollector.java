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

package org.apache.airavata.mft.transport.azure;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobProperties;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.azure.AzureSecret;
import org.apache.airavata.mft.resource.stubs.azure.storage.AzureStorage;

public class AzureMetadataCollector implements MetadataCollector {

    boolean initialized = false;

    private AzureStorage azureStorage;
    private AzureSecret azureSecret;
    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.azureStorage = storage.getAzure();
        this.azureSecret = secret.getAzure();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Azure Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath) throws Exception {
        checkInitialized();

        // Azure does not have a concept called hierarchical containers. So we assume that there are no containers inside
        // the given container
        ResourceMetadata.Builder metadataBuilder = ResourceMetadata.newBuilder();
        if (!isAvailable(resourcePath)) {
            metadataBuilder.setError(MetadataFetchError.NOT_FOUND);
            return metadataBuilder.build();
        }

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(azureSecret.getConnectionString()).buildClient();

        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(azureStorage.getContainer());

        if (resourcePath.isEmpty()) { // List the container
            PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs();
            DirectoryMetadata.Builder directoryBuilder = DirectoryMetadata.newBuilder();
            blobItems.forEach(blobItem -> {
                FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                BlobItemProperties properties = blobItem.getProperties();

                fileBuilder.setResourceSize(properties.getContentLength());
                fileBuilder.setCreatedTime(properties.getCreationTime().toEpochSecond());
                fileBuilder.setUpdateTime(properties.getCreationTime().toEpochSecond());
                fileBuilder.setFriendlyName(blobItem.getName());
                fileBuilder.setResourcePath(blobItem.getName());
                byte[] contentMd5 = properties.getContentMd5();
                StringBuilder md5sb = new StringBuilder();
                for (byte aByte : contentMd5) {
                    md5sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
                }
                fileBuilder.setMd5Sum(md5sb.toString());
                directoryBuilder.addFiles(fileBuilder);
            });
            metadataBuilder.setDirectory(directoryBuilder);

        } else { // If resource is a file

            BlobClient blobClient = blobContainerClient.getBlobClient(resourcePath);
            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
            BlobProperties properties = blobClient.getProperties();

            fileBuilder.setResourceSize(properties.getBlobSize());
            fileBuilder.setCreatedTime(properties.getCreationTime().toEpochSecond());
            fileBuilder.setUpdateTime(properties.getCreationTime().toEpochSecond());
            fileBuilder.setFriendlyName(blobClient.getBlobName());
            fileBuilder.setResourcePath(resourcePath);
            byte[] contentMd5 = properties.getContentMd5();
            StringBuilder md5sb = new StringBuilder();
            for (byte aByte : contentMd5) {
                md5sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            fileBuilder.setMd5Sum(md5sb.toString());
            metadataBuilder.setFile(fileBuilder);
        }

        return metadataBuilder.build();
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {
        checkInitialized();

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(azureSecret.getConnectionString()).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureStorage.getContainer());
        boolean containerExists = containerClient.exists();
        if (!containerExists) {
            return false;
        }
        if (resourcePath.isEmpty()) {
            return true;
        }
        return containerClient.getBlobClient(resourcePath).exists();
    }
}
