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
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.io.ByteStreams;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingChunkedConnector;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.PrivateKey;

public class GCSIncomingChunkedConnector implements IncomingChunkedConnector {

    private static final Logger logger = LoggerFactory.getLogger(GCSIncomingChunkedConnector.class);

    private Blob blob;
    private ConnectorConfig connectorConfig;

    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.connectorConfig = connectorConfig;
        GCSSecret gcsSecret = connectorConfig.getSecret().getGcs();

        GCSStorage gcsStorage = connectorConfig.getStorage().getGcs();

        PrivateKey privKey = GCSUtil.getPrivateKey(gcsSecret.getPrivateKey());

        try (Storage storage = StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.newBuilder()
                .setProjectId(gcsSecret.getProjectId())
                .setPrivateKey(privKey)
                .setClientEmail(gcsSecret.getClientEmail())
                .build()).build().getService()) {

            blob = storage.get(gcsStorage.getBucketName(),
                    connectorConfig.getResourcePath(), Storage.BlobGetOption.fields(Storage.BlobField.values()));
        }
    }

    @Override
    public void complete() throws Exception {
        logger.info("File {} successfully received", connectorConfig.getResourcePath());

    }

    @Override
    public void failed() throws Exception {
        logger.error("Failed while receiving file {}", connectorConfig.getResourcePath());
    }

    @Override
    public void downloadChunk(int chunkId, long startByte, long endByte, String downloadFile) throws Exception {
        try (ReadChannel from = blob.reader();
        FileChannel to = FileChannel.open(Paths.get(downloadFile), StandardOpenOption.WRITE)) {
            from.seek(startByte);
            from.limit(endByte);
            ByteStreams.copy(from, to);
        }
    }

    @Override
    public InputStream downloadChunk(int chunkId, long startByte, long endByte) throws Exception {
        try (ReadChannel from = blob.reader()) {
            from.seek(startByte);
            from.limit(endByte);
            return Channels.newInputStream(from);
        }
    }
}
