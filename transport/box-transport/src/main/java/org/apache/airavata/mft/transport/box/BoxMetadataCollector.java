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
import org.apache.airavata.mft.core.AuthZToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.box.BoxSecret;
import org.apache.airavata.mft.credential.stubs.box.BoxSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.box.resource.BoxResource;
import org.apache.airavata.mft.resource.stubs.box.resource.BoxResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.box.storage.BoxStorage;
import org.apache.airavata.mft.resource.stubs.box.storage.BoxStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
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
    public FileResourceMetadata getFileResourceMetadata(AuthZToken authZToken, String resourceId, String credentialToken) throws Exception {

        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        BoxResource boxResource = resourceClient.box().getBoxResource(BoxResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        BoxSecret boxSecret = secretClient.box().getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());
        BoxFile boxFile = new BoxFile(api, boxResource.getFile().getResourcePath());
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
    public FileResourceMetadata getFileResourceMetadata(AuthZToken authZToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthZToken authZToken, String resourceId, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthZToken authZToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Boolean isAvailable(AuthZToken authZToken, String resourceId, String credentialToken) throws Exception {

        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        BoxResource boxResource = resourceClient.box().getBoxResource(BoxResourceGetRequest.newBuilder().setResourceId(resourceId).build());
        return isAvailable(boxResource, credentialToken);
    }

    @Override
    public Boolean isAvailable(String storageId, String resourcePath, String credentialToken) throws Exception {
        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        BoxStorage boxStorage = resourceClient.box().getBoxStorage(BoxStorageGetRequest.newBuilder().setStorageId(storageId).build());

        BoxResource boxResource = BoxResource.newBuilder().setFile(FileResource.newBuilder().setResourcePath(resourcePath).build()).setBoxStorage(boxStorage).build();

        return isAvailable(boxResource, credentialToken);
    }

    private Boolean isAvailable(BoxResource boxResource, String credentialToken) throws Exception {
        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        BoxSecret boxSecret = secretClient.box().getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());

        BoxFile boxFile;
        switch (boxResource.getResourceCase().name()){
            case ResourceTypes.FILE:
                boxFile = new BoxFile(api, boxResource.getFile().getResourcePath());
            case ResourceTypes.DIRECTORY:
                boxFile = new BoxFile(api, boxResource.getDirectory().getResourcePath());
        }
        return true;
    }
}
