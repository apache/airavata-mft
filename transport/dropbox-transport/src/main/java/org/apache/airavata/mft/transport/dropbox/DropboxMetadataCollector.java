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
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecret;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.DropboxStorage;

import java.util.List;

public class DropboxMetadataCollector implements MetadataCollector {

    private DropboxStorage dropboxStorage;
    private DropboxSecret dropboxSecret;

    boolean initialized = false;

    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.dropboxStorage = storage.getDropbox();
        this.dropboxSecret = secret.getDropbox();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Dropbox Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath) throws Exception {
        checkInitialized();

        DbxRequestConfig config = DbxRequestConfig.newBuilder("mftdropbox/v1").build();
        DbxClientV2 dbxClientV2 = new DbxClientV2(config, dropboxSecret.getAccessToken());

        ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();

        if (resourcePath.isEmpty()) { // List root folder
            ListFolderResult listFolderResult = dbxClientV2.files().listFolder(resourcePath);
            List<Metadata> entryMetadatas = listFolderResult.getEntries();
            DirectoryMetadata.Builder directoryBuilder = DirectoryMetadata.newBuilder();
            directoryBuilder.setResourcePath("");
            directoryBuilder.setFriendlyName("");

            entryMetadatas.forEach(entryMetadata -> {
                if (entryMetadata instanceof com.dropbox.core.v2.files.FileMetadata) {
                    com.dropbox.core.v2.files.FileMetadata fileMetadata = (com.dropbox.core.v2.files.FileMetadata) entryMetadata;
                    FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                    fileBuilder.setResourceSize(fileMetadata.getSize());
                    fileBuilder.setUpdateTime(fileMetadata.getServerModified().getTime());
                    fileBuilder.setCreatedTime(fileMetadata.getClientModified().getTime());
                    fileBuilder.setFriendlyName(fileMetadata.getName());
                    fileBuilder.setResourcePath(resourcePath);
                    directoryBuilder.addFiles(fileBuilder);
                } else if (entryMetadata instanceof FolderMetadata) {
                    FolderMetadata folderMetadata = (FolderMetadata) entryMetadata;
                    DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                    subDirBuilder.setFriendlyName(folderMetadata.getName());
                    subDirBuilder.setResourcePath(folderMetadata.getName());
                    directoryBuilder.addDirectories(subDirBuilder);
                }
            });

            resourceBuilder.setDirectory(directoryBuilder);

        } else {
            Metadata entryMetadata = dbxClientV2.files().getMetadata(resourcePath);
            if (entryMetadata instanceof com.dropbox.core.v2.files.FileMetadata) {
                com.dropbox.core.v2.files.FileMetadata fileMetadata = (com.dropbox.core.v2.files.FileMetadata) entryMetadata;
                FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                fileBuilder.setResourceSize(fileMetadata.getSize());
                fileBuilder.setUpdateTime(fileMetadata.getServerModified().getTime());
                fileBuilder.setCreatedTime(fileMetadata.getClientModified().getTime());
                fileBuilder.setFriendlyName(fileMetadata.getName());
                fileBuilder.setResourcePath(resourcePath);
                resourceBuilder.setFile(fileBuilder);
            } else if (entryMetadata instanceof FolderMetadata) {

                ListFolderResult listFolderResult = dbxClientV2.files().listFolder(resourcePath);
                List<Metadata> entryMetadatas = listFolderResult.getEntries();
                DirectoryMetadata.Builder directoryBuilder = DirectoryMetadata.newBuilder();
                directoryBuilder.setResourcePath(resourcePath);
                directoryBuilder.setFriendlyName(entryMetadata.getName());

                entryMetadatas.forEach(em -> {
                    if (em instanceof com.dropbox.core.v2.files.FileMetadata) {
                        com.dropbox.core.v2.files.FileMetadata fileMetadata = (com.dropbox.core.v2.files.FileMetadata) em;
                        FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                        fileBuilder.setResourceSize(fileMetadata.getSize());
                        fileBuilder.setUpdateTime(fileMetadata.getServerModified().getTime());
                        fileBuilder.setCreatedTime(fileMetadata.getClientModified().getTime());
                        fileBuilder.setFriendlyName(fileMetadata.getName());
                        fileBuilder.setResourcePath(resourcePath + "/" + fileMetadata.getName());
                        directoryBuilder.addFiles(fileBuilder);
                    } else if (em instanceof FolderMetadata) {
                        FolderMetadata folderMetadata = (FolderMetadata) em;
                        DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                        subDirBuilder.setFriendlyName(folderMetadata.getName());
                        subDirBuilder.setResourcePath(resourcePath + "/" + folderMetadata.getName());
                        directoryBuilder.addDirectories(subDirBuilder);
                    }
                    resourceBuilder.setDirectory(directoryBuilder);
                });
            } else {
                resourceBuilder.setError(MetadataFetchError.NOT_FOUND);
            }
        }

        return resourceBuilder.build();
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {
        checkInitialized();

        DbxRequestConfig config = DbxRequestConfig.newBuilder("mftdropbox/v1").build();
        DbxClientV2 dbxClientV2 = new DbxClientV2(config, dropboxSecret.getAccessToken());

        return !dbxClientV2.files().searchV2(resourcePath).getMatches().isEmpty();
    }
}
