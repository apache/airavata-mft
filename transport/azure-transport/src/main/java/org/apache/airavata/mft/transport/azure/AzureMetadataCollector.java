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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.azure.AzureSecret;
import org.apache.airavata.mft.credential.stubs.azure.AzureSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.azure.resource.AzureResource;
import org.apache.airavata.mft.resource.stubs.azure.resource.AzureResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.azure.storage.AzureStorage;
import org.apache.airavata.mft.resource.stubs.azure.storage.AzureStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;

public class AzureMetadataCollector implements MetadataCollector {

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
            throw new IllegalStateException("Azure Metadata Collector is not initialized");
        }
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(String resourceId, String credentialToken) throws Exception {
        checkInitialized();

        if (!isAvailable(resourceId, credentialToken)) {
            throw new Exception("Azure blob can not find for resource id " + resourceId);
        }

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        AzureResource azureResource = resourceClient.azure().getAzureResource(AzureResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        AzureSecret azureSecret = secretClient.azure().getAzureSecret(AzureSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(azureSecret.getConnectionString()).buildClient();

        BlobClient blobClient = blobServiceClient.getBlobContainerClient(azureResource.getAzureStorage().getContainer())
                                                .getBlobClient(azureResource.getFile().getResourcePath());

        BlobProperties properties = blobClient.getBlockBlobClient().getProperties();
        FileResourceMetadata metadata = new FileResourceMetadata();
        metadata.setResourceSize(properties.getBlobSize());
        metadata.setCreatedTime(properties.getCreationTime().toEpochSecond());
        metadata.setUpdateTime(properties.getCreationTime().toEpochSecond());

        byte[] contentMd5 = properties.getContentMd5();
        StringBuilder md5sb = new StringBuilder();
        for (byte aByte : contentMd5) {
            md5sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        metadata.setMd5sum(md5sb.toString());

        return metadata;
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(String storageId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(String resourceId, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(String storageId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {
        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        AzureResource azureResource = resourceClient.azure().getAzureResource(AzureResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        return isAvailable(azureResource, credentialToken);
    }

    @Override
    public Boolean isAvailable(String storageId, String resourcePath, String credentialToken) throws Exception {
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        AzureStorage azureStorage = resourceClient.azure().getAzureStorage(AzureStorageGetRequest.newBuilder().setStorageId(storageId).build());

        AzureResource azureResource = AzureResource.newBuilder().setFile(FileResource.newBuilder().setResourcePath(resourcePath).build()).setAzureStorage(azureStorage).build();
        return isAvailable(azureResource, credentialToken);
    }

    public Boolean isAvailable(AzureResource azureResource, String credentialToken) throws Exception {

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        AzureSecret azureSecret = secretClient.azure().getAzureSecret(AzureSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(azureSecret.getConnectionString()).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureResource.getAzureStorage().getContainer());
        boolean containerExists = containerClient.exists();
        if (!containerExists) {
            return false;
        }
        switch (azureResource.getResourceCase().name()){
            case ResourceTypes.FILE:
                return containerClient.getBlobClient(azureResource.getFile().getResourcePath()).exists();
            case ResourceTypes.DIRECTORY:
                return containerClient.getBlobClient(azureResource.getDirectory().getResourcePath()).exists();
        }
        return false;
    }

}
