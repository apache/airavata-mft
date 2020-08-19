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
import com.azure.storage.blob.specialized.BlobInputStream;
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

import java.io.OutputStream;

public class AzureReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(AzureReceiver.class);

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
            throw new IllegalStateException("Azure Receiver is not initialized");
        }
    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        logger.info("Starting azure receive for remote server for transfer {}", context.getTransferId());
        checkInitialized();

        if (ResourceTypes.FILE.equals(this.azureResource.getResourceCase().name())) {
            BlobClient blobClient = containerClient.getBlobClient(azureResource.getFile().getResourcePath());
            BlobInputStream blobInputStream = blobClient.openInputStream();

            OutputStream streamOs = context.getStreamBuffer().getOutputStream();

            long fileSize = context.getMetadata().getResourceSize();

            byte[] buf = new byte[1024];
            while (true) {
                int bufSize = 0;

                if (buf.length < fileSize) {
                    bufSize = buf.length;
                } else {
                    bufSize = (int) fileSize;
                }
                bufSize = blobInputStream.read(buf, 0, bufSize);

                if (bufSize < 0) {
                    break;
                }

                streamOs.write(buf, 0, bufSize);
                streamOs.flush();

                fileSize -= bufSize;
                if (fileSize == 0L)
                    break;
            }

            streamOs.close();
            logger.info("Completed azure receive for remote server for transfer {}", context.getTransferId());

        } else {
            logger.error("Resource {} should be a FILE type. Found a {}",
                    this.azureResource.getResourceId(), this.azureResource.getResourceCase().name());
            throw new Exception("Resource " + this.azureResource.getResourceId() + " should be a FILE type. Found a " +
                    this.azureResource.getResourceCase().name());
        }
    }
}
