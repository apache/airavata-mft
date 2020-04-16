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
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.AzureResource;
import org.apache.airavata.mft.resource.service.AzureResourceGetRequest;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.AzureSecret;
import org.apache.airavata.mft.secret.service.AzureSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;

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
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {
        checkInitialized();

        if (!isAvailable(resourceId, credentialToken)) {
            throw new Exception("Azure blob can not find for resource id " + resourceId);
        }

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        AzureResource azureResource = resourceClient.getAzureResource(AzureResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        AzureSecret azureSecret = secretClient.getAzureSecret(AzureSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(azureSecret.getConnectionString()).buildClient();

        BlobClient blobClient = blobServiceClient.getBlobContainerClient(azureResource.getContainer()).getBlobClient(azureResource.getBlobName());

        BlobProperties properties = blobClient.getBlockBlobClient().getProperties();
        ResourceMetadata metadata = new ResourceMetadata();
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
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {
        checkInitialized();

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        AzureResource azureResource = resourceClient.getAzureResource(AzureResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        AzureSecret azureSecret = secretClient.getAzureSecret(AzureSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(azureSecret.getConnectionString()).buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(azureResource.getContainer());
        boolean containerExists = containerClient.exists();
        if (!containerExists) {
            return false;
        }
        return containerClient.getBlobClient(azureResource.getBlobName()).exists();
    }
}
