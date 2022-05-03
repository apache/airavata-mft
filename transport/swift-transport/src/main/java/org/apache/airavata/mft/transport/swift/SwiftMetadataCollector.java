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

package org.apache.airavata.mft.transport.swift;

import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.GenericResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.jclouds.ContextBuilder;
import org.jclouds.openstack.keystone.auth.config.CredentialTypes;
import org.jclouds.openstack.keystone.config.KeystoneProperties;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.ObjectApi;

import java.util.Properties;

public class SwiftMetadataCollector implements MetadataCollector {

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
            throw new IllegalStateException("Swift Metadata Collector is not initialized");
        }
    }

    private SwiftApi getSwiftApi(SwiftStorage swiftStorage, SwiftSecret swiftSecret) {
        String provider = "openstack-swift";

        Properties overrides = new Properties();
        overrides.put(KeystoneProperties.KEYSTONE_VERSION, swiftStorage.getKeystoneVersion() + "");

        String identity = null;
        String credential = null;
        switch (swiftSecret.getSecretCase()) {
            case PASSWORDSECRET:
                identity = swiftSecret.getPasswordSecret().getDomainId() + ":" + swiftSecret.getPasswordSecret().getUserName();
                credential = swiftSecret.getPasswordSecret().getPassword();
                overrides.put(KeystoneProperties.SCOPE, "projectId:" + swiftSecret.getPasswordSecret().getProjectId());
                overrides.put(KeystoneProperties.CREDENTIAL_TYPE, CredentialTypes.PASSWORD_CREDENTIALS);
                break;
            case AUTHCREDENTIALSECRET:
                identity = swiftSecret.getAuthCredentialSecret().getCredentialId();
                credential = swiftSecret.getAuthCredentialSecret().getCredentialSecret();
                overrides.put(KeystoneProperties.CREDENTIAL_TYPE, CredentialTypes.API_ACCESS_KEY_CREDENTIALS);
                break;
        }

        return ContextBuilder.newBuilder(provider)
                .endpoint(swiftStorage.getEndpoint())
                .credentials(identity, credential)
                .overrides(overrides)
                .buildApi(SwiftApi.class);
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {
        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        GenericResource swiftResource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        SwiftSecret swiftSecret = secretClient.swift().getSwiftSecret(SwiftSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        SwiftApi swiftApi = getSwiftApi(swiftResource.getSwiftStorage(), swiftSecret);

        ObjectApi objectApi = swiftApi.getObjectApi(swiftResource.getSwiftStorage().getRegion(), swiftResource.getSwiftStorage().getContainer());

        SwiftObject swiftObject = objectApi.get(swiftResource.getFile().getResourcePath());

        FileResourceMetadata metadata = new FileResourceMetadata();
        metadata.setResourceSize(swiftObject.getPayload().getContentMetadata().getContentLength());
        metadata.setMd5sum(swiftObject.getETag());
        metadata.setUpdateTime(swiftObject.getLastModified().getTime());
        metadata.setCreatedTime(swiftObject.getLastModified().getTime());
        return metadata;
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Boolean isAvailable(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {
        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        GenericResource swiftResource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        SwiftSecret swiftSecret = secretClient.swift().getSwiftSecret(SwiftSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        SwiftApi swiftApi = getSwiftApi(swiftResource.getSwiftStorage(), swiftSecret);

        ObjectApi objectApi = swiftApi.getObjectApi(swiftResource.getSwiftStorage().getRegion(), swiftResource.getSwiftStorage().getContainer());

        SwiftObject swiftObject = objectApi.get(swiftResource.getFile().getResourcePath());

        return swiftObject != null;
    }

    @Override
    public Boolean isAvailable(AuthToken authToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
