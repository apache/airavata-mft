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


import com.box.sdk.*;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.box.BoxSecret;
import org.apache.airavata.mft.resource.stubs.box.storage.BoxStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxMetadataCollector implements MetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(BoxMetadataCollector.class);

    boolean initialized = false;

    private BoxStorage boxStorage;
    private BoxSecret boxSecret;
    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.boxStorage = storage.getBox();
        this.boxSecret = secret.getBox();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("S3 Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {

        checkInitialized();

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());

        BoxFile boxFile = null;
        BoxFile.Info boxFileInfo = null;
        BoxFolder boxFolder = null;
        BoxFolder.Info boxFolderInfo;
        boolean isFile = true;

        ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();

        try { // There is no API to derive the type of entry. So, manually checking the file type is used
            boxFile = new BoxFile(api, resourcePath);
            boxFileInfo = boxFile.getInfo();
        } catch (BoxAPIException e) {
            try {
                boxFolder = new BoxFolder(api, resourcePath);
                boxFolderInfo = boxFolder.getInfo();
                isFile = false;
            } catch (BoxAPIException ex) {
                logger.error("Failed to fetch information of box item {}", resourcePath, ex);
                resourceBuilder.setError(MetadataFetchError.NOT_FOUND);
                return resourceBuilder.build();
            }
        }

        if (isFile) {
            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
            fileBuilder.setResourcePath(resourcePath);
            fileBuilder.setFriendlyName(boxFileInfo.getName());
            fileBuilder.setResourceSize(boxFileInfo.getSize());
            fileBuilder.setCreatedTime(boxFileInfo.getCreatedAt().getTime());
            fileBuilder.setUpdateTime(boxFileInfo.getModifiedAt().getTime());
            resourceBuilder.setFile(fileBuilder);
        } else {
            DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();

            Iterable<BoxItem.Info> itemsInFolder = boxFolder.getChildren("metadata.global.properties");
            for (BoxItem.Info itemInfo : itemsInFolder) {
                if (itemInfo instanceof BoxFile.Info) {
                    BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
                    FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                    fileBuilder.setResourcePath(fileInfo.getID());
                    fileBuilder.setFriendlyName(fileInfo.getName());
                    fileBuilder.setResourceSize(fileInfo.getSize());
                    fileBuilder.setCreatedTime(fileInfo.getCreatedAt().getTime());
                    fileBuilder.setUpdateTime(fileInfo.getModifiedAt().getTime());
                    dirBuilder.addFiles(fileBuilder);
                } else if (itemInfo instanceof BoxFolder.Info) {
                    BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
                    DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                    subDirBuilder.setFriendlyName(folderInfo.getName());
                    subDirBuilder.setResourcePath(folderInfo.getID());
                    subDirBuilder.setCreatedTime(folderInfo.getCreatedAt().getTime());
                    subDirBuilder.setUpdateTime(folderInfo.getModifiedAt().getTime());
                    dirBuilder.addDirectories(subDirBuilder);
                }
            }
            resourceBuilder.setDirectory(dirBuilder);
        }
        return resourceBuilder.build();
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {

        checkInitialized();

        BoxAPIConnection api = new BoxAPIConnection(boxSecret.getAccessToken());
        try {
            BoxFile boxFile = new BoxFile(api, resourcePath);
            boxFile.getInfo();
        } catch (BoxAPIException e) {
            try {
                BoxFolder boxFolder = new BoxFolder(api, resourcePath);
                boxFolder.getInfo();
            } catch (BoxAPIException ex) {
                logger.warn("Failed to fetch information of box item {}", resourcePath, ex);
                return false;
            }
        }
        return true;
    }
}
