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

package org.apache.airavata.mft.transport.box;


import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.box.BoxSecret;
import org.apache.airavata.mft.credential.stubs.box.BoxSecretGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;

public class BoxMetadataCollector implements MetadataCollector {

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
            throw new IllegalStateException("S3 Metadata Collector is not initialized");
        }
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {

        checkInitialized();

        BoxSecret boxSecret;
        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                secretServiceHost, secretServicePort)) {
            boxSecret = secretClient.box().getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        }

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());
        BoxFile boxFile = new BoxFile(api, resourcePath);
        BoxFile.Info boxFileInfo = boxFile.getInfo();

        FileResourceMetadata metadata = new FileResourceMetadata();
        metadata.setResourceSize(boxFileInfo.getSize());

        // TODO
        // metadata.setMd5sum(boxFileInfo.getSha1());

        metadata.setUpdateTime(boxFileInfo.getModifiedAt().getTime());
        metadata.setCreatedTime(boxFileInfo.getCreatedAt().getTime());

        return metadata;
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Boolean isAvailable(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {

        checkInitialized();

        BoxSecret boxSecret;
        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                secretServiceHost, secretServicePort)) {
            boxSecret = secretClient.box().getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        }

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());
        BoxFile boxFile = new BoxFile(api, resourcePath);
        // TODO Fix this. Need to figur out how to check if the file exists in box
        return true;
    }
}
