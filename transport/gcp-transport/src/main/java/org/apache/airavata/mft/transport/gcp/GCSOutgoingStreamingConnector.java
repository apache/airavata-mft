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

package org.apache.airavata.mft.transport.gcp;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.OutgoingStreamingConnector;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.security.PrivateKey;

public class GCSOutgoingStreamingConnector implements OutgoingStreamingConnector {

    private static final Logger logger = LoggerFactory.getLogger(GCSOutgoingStreamingConnector.class);

    private Storage storage;
    private BlobInfo blobInfo;
    private ConnectorConfig connectorConfig;

    private WriteChannel writer;
    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {

        this.connectorConfig = connectorConfig;
        GCSSecret gcsSecret = connectorConfig.getSecret().getGcs();

        GCSStorage gcsStorage = connectorConfig.getStorage().getGcs();

        PrivateKey privKey = GCSUtil.getPrivateKey(gcsSecret.getPrivateKey());

        storage = StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.newBuilder()
                .setProjectId(gcsSecret.getProjectId())
                .setPrivateKey(privKey)
                .setClientEmail(gcsSecret.getClientEmail())
                .build()).build().getService();

        BlobId blobId = BlobId.of(gcsStorage.getBucketName(), connectorConfig.getResourcePath());
        blobInfo = BlobInfo.newBuilder(blobId).build();

    }

    @Override
    public void complete() throws Exception {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                logger.warn("Failed to close the writer", e);
            }
        }
        if (storage != null) {
            try {
                this.storage.close();
            } catch (Exception e) {
                logger.warn("Failed to close the storage", e);
            }
        }
        logger.info("File {} successfully sent", connectorConfig.getResourcePath());
    }

    @Override
    public void failed() throws Exception {
        if (writer != null) {
            try {
                writer.close();
            } catch (Exception e) {
                logger.warn("Failed to close the writer", e);
            }
        }
        if (storage != null) {
            try {
                this.storage.close();
            } catch (Exception e) {
                logger.warn("Failed to close the storage", e);
            }
        }
        logger.info("File {} failed to send", connectorConfig.getResourcePath());
    }

    @Override
    public OutputStream fetchOutputStream() throws Exception {
        writer = storage.writer(blobInfo);
        return Channels.newOutputStream(writer);
    }
}
