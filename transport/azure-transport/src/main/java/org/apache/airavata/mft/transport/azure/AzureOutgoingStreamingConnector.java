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
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.OutgoingStreamingConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class AzureOutgoingStreamingConnector implements OutgoingStreamingConnector {

    private static final Logger logger = LoggerFactory.getLogger(AzureOutgoingStreamingConnector.class);

    private ConnectorConfig connectorConfig;
    private OutputStream os;

    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.connectorConfig = connectorConfig;
    }

    @Override
    public void complete() throws Exception {
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
                logger.warn("Failed to close the output stream", e);
            }
        }
    }

    @Override
    public void failed() throws Exception {
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
                logger.warn("Failed to close the output stream", e);
            }
        }
    }

    @Override
    public OutputStream fetchOutputStream() throws Exception {

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectorConfig.getSecret().getAzure().getConnectionString()).buildClient();
        BlobContainerClient blobContainerClient = blobServiceClient
                .getBlobContainerClient(connectorConfig.getStorage().getAzure().getContainer());

        BlobClient blobClient = blobContainerClient.getBlobClient(connectorConfig.getResourcePath());

        os =  blobClient.getBlockBlobClient().getBlobOutputStream(true);
        return os;
    }
}
