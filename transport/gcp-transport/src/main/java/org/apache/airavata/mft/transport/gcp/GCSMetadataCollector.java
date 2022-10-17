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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecretGetRequest;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

public class GCSMetadataCollector implements MetadataCollector {

    boolean initialized = false;
    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;

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
            throw new IllegalStateException("GCS Metadata Collector is not initialized");
        }
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {
        checkInitialized();

        GCSStorage gcsStorage;
        try (StorageServiceClient storageServiceClient = StorageServiceClientBuilder
                .buildClient(resourceServiceHost, resourceServicePort)) {

            gcsStorage = storageServiceClient.gcs()
                    .getGCSStorage(GCSStorageGetRequest.newBuilder().setStorageId(storageId).build());
        }

        GCSSecret gcsSecret;
        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                secretServiceHost, secretServicePort)) {
            gcsSecret = secretClient.gcs().getGCSSecret(GCSSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        }

        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString = gcsSecret.getCredentialsJson();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), transport, jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }

        Storage storage = new Storage.Builder(transport, jsonFactory, credential).build();

        FileResourceMetadata metadata = new FileResourceMetadata();
        StorageObject gcsMetadata = storage.objects().get(gcsStorage.getBucketName(), resourcePath).execute();

        metadata.setResourceSize(gcsMetadata.getSize().longValue());
        String md5Sum = String.format("%032x", new BigInteger(1, Base64.getDecoder().decode(gcsMetadata.getMd5Hash())));
        metadata.setMd5sum(md5Sum);
        metadata.setUpdateTime(gcsMetadata.getTimeStorageClassUpdated().getValue());
        metadata.setCreatedTime(gcsMetadata.getTimeCreated().getValue());
        return metadata;
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String resourcePath,
                                                                  String storageId, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public Boolean isAvailable(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {
        checkInitialized();

        GCSStorage gcsStorage;
        try (StorageServiceClient storageServiceClient = StorageServiceClientBuilder
                .buildClient(resourceServiceHost, resourceServicePort)) {

            gcsStorage = storageServiceClient.gcs()
                    .getGCSStorage(GCSStorageGetRequest.newBuilder().setStorageId(storageId).build());
        }

        GCSSecret gcsSecret;
        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                secretServiceHost, secretServicePort)) {
            gcsSecret = secretClient.gcs().getGCSSecret(GCSSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        }

        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString = gcsSecret.getCredentialsJson();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), transport, jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }

        Storage storage = new Storage.Builder(transport, jsonFactory, credential).build();
        return !storage.objects().get(gcsStorage.getBucketName(), resourcePath).execute().isEmpty();
    }
}
