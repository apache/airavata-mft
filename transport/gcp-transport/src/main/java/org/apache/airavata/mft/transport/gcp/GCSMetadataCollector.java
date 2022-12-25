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

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;

import java.security.PrivateKey;
import java.time.temporal.ChronoField;

public class GCSMetadataCollector implements MetadataCollector {

    boolean initialized = false;

    private GCSStorage gcsStorage;
    private GCSSecret gcsSecret;

    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.gcsStorage = storage.getGcs();
        this.gcsSecret = secret.getGcs();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("GCS Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {
        checkInitialized();

        PrivateKey privKey = GCSUtil.getPrivateKey(gcsSecret.getPrivateKey());

        try (Storage storage = StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.newBuilder()
                .setProjectId(gcsSecret.getProjectId())
                .setPrivateKey(privKey)
                .setClientEmail(gcsSecret.getClientEmail())
                .build()).build().getService()) {

            Blob blob = storage.get(gcsStorage.getBucketName(),
                    resourcePath, Storage.BlobGetOption.fields(Storage.BlobField.values()));


            ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();
            if (blob != null) {
                FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                fileBuilder.setFriendlyName(blob.getName());
                fileBuilder.setResourcePath(resourcePath);
                fileBuilder.setCreatedTime(blob.getCreateTimeOffsetDateTime().getLong(ChronoField.INSTANT_SECONDS));
                fileBuilder.setUpdateTime(blob.getUpdateTimeOffsetDateTime().getLong(ChronoField.INSTANT_SECONDS));
                fileBuilder.setResourceSize(blob.getSize());
                fileBuilder.setMd5Sum(blob.getMd5());
                resourceBuilder.setFile(fileBuilder);
            } else {
                final String dirPath = resourcePath.endsWith("/") ? resourcePath : resourcePath + "/";

                DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();

                try {

                    Page<Blob> blobs = storage.list(gcsStorage.getBucketName(),
                            Storage.BlobListOption.currentDirectory(),
                            Storage.BlobListOption.prefix(dirPath));
                    Iterable<Blob> blobIter = blobs.iterateAll();
                    blobIter.forEach(b -> {
                        if (b.isDirectory()) {
                            DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                            subDirBuilder.setCreatedTime(b.getCreateTimeOffsetDateTime().getLong(ChronoField.INSTANT_SECONDS));
                            subDirBuilder.setUpdateTime(b.getUpdateTimeOffsetDateTime().getLong(ChronoField.INSTANT_SECONDS));
                            dirBuilder.addDirectories(subDirBuilder);
                        } else {
                            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                            fileBuilder.setFriendlyName(b.getName());
                            fileBuilder.setResourcePath(dirPath + b.getName());
                            fileBuilder.setCreatedTime(b.getCreateTimeOffsetDateTime().getLong(ChronoField.INSTANT_SECONDS));
                            fileBuilder.setUpdateTime(b.getUpdateTimeOffsetDateTime().getLong(ChronoField.INSTANT_SECONDS));
                            fileBuilder.setResourceSize(b.getSize());
                            fileBuilder.setMd5Sum(b.getMd5());
                            dirBuilder.addFiles(fileBuilder);
                        }
                    });


                    resourceBuilder.setDirectory(dirBuilder);
                } catch (Exception e) {
                    resourceBuilder.setError(MetadataFetchError.NOT_FOUND);
                }
            }
            return resourceBuilder.build();
        }
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {
        checkInitialized();
        PrivateKey privKey = GCSUtil.getPrivateKey(gcsSecret.getPrivateKey());

        try (Storage storage = StorageOptions.newBuilder().setCredentials(ServiceAccountCredentials.newBuilder()
                .setProjectId(gcsSecret.getProjectId())
                .setPrivateKey(privKey)
                .setClientEmail(gcsSecret.getClientEmail())
                .build()).build().getService()) {

            Blob blob = storage.get(gcsStorage.getBucketName(),
                    resourcePath, Storage.BlobGetOption.fields(Storage.BlobField.values()));

            if (blob != null) {
                return true;
            } else {
                final String dirPath = resourcePath.endsWith("/") ? resourcePath : resourcePath + "/";
                try {

                    Page<Blob> blobs = storage.list(gcsStorage.getBucketName(),
                            Storage.BlobListOption.currentDirectory(),
                            Storage.BlobListOption.prefix(dirPath));

                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }
}
