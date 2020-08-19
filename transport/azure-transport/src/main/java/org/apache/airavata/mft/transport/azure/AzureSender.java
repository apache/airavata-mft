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

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.credential.stubs.azure.AzureSecret;
import org.apache.airavata.mft.credential.stubs.azure.AzureSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.azure.resource.AzureResource;
import org.apache.airavata.mft.resource.stubs.azure.resource.AzureResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureSender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(AzureSender.class);

    private boolean initialized = false;
    private AzureResource azureResource;
    BlobContainerClient containerClient;

    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {
        this.initialized = true;

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        this.azureResource = resourceClient.azure().getAzureResource(AzureResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        AzureSecret azureSecret = secretClient.azure().getAzureSecret(AzureSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(azureSecret.getConnectionString()).buildClient();
        this.containerClient = blobServiceClient.getBlobContainerClient(azureResource.getAzureStorage().getContainer());
    }

    @Override
    public void destroy() {

    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Azure Sender is not initialized");
        }
    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        logger.info("Starting Azure send for remote server for transfer {}", context.getTransferId());
        checkInitialized();

        if (ResourceTypes.FILE.equals(this.azureResource.getResourceCase().name())) {
            BlockBlobClient blockBlobClient = containerClient.getBlobClient(azureResource.getFile().getResourcePath()).getBlockBlobClient();
            blockBlobClient.upload(context.getStreamBuffer().getInputStream(), context.getMetadata().getResourceSize(), true);
            logger.info("Completed Azure send for remote server for transfer {}", context.getTransferId());
        } else {
            logger.error("Resource {} should be a FILE type. Found a {}",
                    this.azureResource.getResourceId(), this.azureResource.getResourceCase().name());
            throw new Exception("Resource " + this.azureResource.getResourceId() + " should be a FILE type. Found a " +
                    this.azureResource.getResourceCase().name());
        }

    }
}
