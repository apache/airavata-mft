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

package org.apache.airavata.mft.transport.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecret;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
import org.apache.airavata.mft.resource.stubs.dropbox.resource.DropboxResource;
import org.apache.airavata.mft.resource.stubs.dropbox.resource.DropboxResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.DropboxStorage;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.DropboxStorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;

public class DropboxMetadataCollector implements MetadataCollector {

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
            throw new IllegalStateException("Dropbox Metadata Collector is not initialized");
        }
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {
        checkInitialized();
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        DropboxResource dropboxResource = resourceClient.dropbox().getDropboxResource(DropboxResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        DropboxSecret dropboxSecret = secretClient.dropbox().getDropboxSecret(DropboxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        DbxRequestConfig config = DbxRequestConfig.newBuilder("mftdropbox/v1").build();
        DbxClientV2 dbxClientV2 = new DbxClientV2(config, dropboxSecret.getAccessToken());

        FileResourceMetadata metadata = new FileResourceMetadata();
        FileMetadata fileMetadata = (FileMetadata) dbxClientV2.files().getMetadata(dropboxResource.getFile().getResourcePath());
        metadata.setResourceSize(fileMetadata.getSize());
        metadata.setMd5sum(null);
        metadata.setUpdateTime(fileMetadata.getServerModified().getTime());
        metadata.setCreatedTime(fileMetadata.getClientModified().getTime());
        return metadata;
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Boolean isAvailable(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {
        checkInitialized();
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        DropboxResource dropboxResource = resourceClient.dropbox().getDropboxResource(DropboxResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        return isAvailable(dropboxResource, credentialToken);
    }

    @Override
    public Boolean isAvailable(AuthToken authToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        DropboxStorage dropboxStorage = resourceClient.dropbox().getDropboxStorage(DropboxStorageGetRequest.newBuilder().setStorageId(parentResourceId).build());

        DropboxResource dropboxResource = DropboxResource.newBuilder()
                .setFile(FileResource.newBuilder().setResourcePath(resourcePath).build())
                .setDropboxStorage(dropboxStorage).build();

        return isAvailable(dropboxResource, credentialToken);
    }

    private Boolean isAvailable(DropboxResource dropboxResource, String credentialToken) throws Exception {
        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        DropboxSecret dropboxSecret = secretClient.dropbox().getDropboxSecret(DropboxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        DbxRequestConfig config = DbxRequestConfig.newBuilder("mftdropbox/v1").build();
        DbxClientV2 dbxClientV2 = new DbxClientV2(config, dropboxSecret.getAccessToken());

        switch (dropboxResource.getResourceCase().name()){
            case ResourceTypes.FILE:
                return !dbxClientV2.files().searchV2(dropboxResource.getFile().getResourcePath()).getMatches().isEmpty();
            case ResourceTypes.DIRECTORY:
                return !dbxClientV2.files().searchV2(dropboxResource.getDirectory().getResourcePath()).getMatches().isEmpty();
        }
        return false;
    }
}
