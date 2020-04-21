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
import com.box.sdk.Metadata;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.BoxResource;
import org.apache.airavata.mft.resource.service.BoxResourceGetRequest;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.BoxSecret;
import org.apache.airavata.mft.secret.service.BoxSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;


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
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {

        checkInitialized();

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        BoxResource boxResource = resourceClient.getBoxResource(BoxResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        BoxSecret boxSecret = secretClient.getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());
        BoxFile boxFile = new BoxFile(api, boxResource.getBoxFileId());
        BoxFile.Info boxFileInfo = boxFile.getInfo();

        ResourceMetadata metadata = new ResourceMetadata();
        metadata.setResourceSize(boxFileInfo.getSize());

        // TODO
        // metadata.setMd5sum(boxFileInfo.getSha1());

        metadata.setUpdateTime(boxFileInfo.getModifiedAt().getTime());
        metadata.setCreatedTime(boxFileInfo.getCreatedAt().getTime());

        return metadata;
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {

        checkInitialized();

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        BoxResource boxResource = resourceClient.getBoxResource(BoxResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        BoxSecret boxSecret = secretClient.getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());
        BoxFile boxFile = new BoxFile(api, boxResource.getBoxFileId());

        return true;
    }
}
