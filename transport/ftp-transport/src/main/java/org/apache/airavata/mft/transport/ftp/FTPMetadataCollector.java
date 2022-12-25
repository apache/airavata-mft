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

package org.apache.airavata.mft.transport.ftp;

import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.ftp.FTPSecret;
import org.apache.airavata.mft.resource.stubs.ftp.storage.FTPStorage;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class FTPMetadataCollector implements MetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(FTPMetadataCollector.class);
    private boolean initialized = false;
    private FTPStorage ftpStorage;
    private FTPSecret ftpSecret;
    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.ftpStorage = storage.getFtp();
        this.ftpSecret = secret.getFtp();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("FTP Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {

        checkInitialized();
        ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();
        FTPClient ftpClient = null;
        try {
            ftpClient = FTPTransportUtil.getFTPClient(ftpStorage, ftpSecret);
            logger.info("Fetching metadata for resource {} in {}", resourcePath, ftpStorage.getHost());

            FTPFile ftpFile = ftpClient.mlistFile(resourcePath);
            if (ftpFile == null) {
                resourceBuilder.setError(MetadataFetchError.NOT_FOUND);
                return resourceBuilder.build();
            }

            if (ftpFile.isDirectory()) {

                DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();
                FTPFile[] ftpFiles = ftpClient.listFiles(resourcePath);
                Arrays.stream(ftpFiles).forEach(child -> {
                    if (child.isFile()) {
                        FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                        fileBuilder.setResourcePath(resourcePath + "/" + child.getName());
                        fileBuilder.setFriendlyName(child.getName());
                        fileBuilder.setCreatedTime(child.getTimestamp().getTimeInMillis());
                        fileBuilder.setUpdateTime(child.getTimestamp().getTimeInMillis());
                        fileBuilder.setResourceSize(child.getSize());
                        dirBuilder.addFiles(fileBuilder);
                    } else if (child.isFile()) {
                        DirectoryMetadata.Builder childDirBuilder = DirectoryMetadata.newBuilder();
                        childDirBuilder.setResourcePath(resourcePath + "/" + child.getName());
                        childDirBuilder.setFriendlyName(child.getName());
                        childDirBuilder.setCreatedTime(child.getTimestamp().getTimeInMillis());
                        childDirBuilder.setUpdateTime(child.getTimestamp().getTimeInMillis());
                        dirBuilder.addDirectories(childDirBuilder);
                    }
                });
                resourceBuilder.setDirectory(dirBuilder);
            } else if (ftpFile.isFile()) {
                FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                fileBuilder.setResourcePath(resourcePath);
                fileBuilder.setFriendlyName(ftpFile.getName());
                fileBuilder.setCreatedTime(ftpFile.getTimestamp().getTimeInMillis());
                fileBuilder.setUpdateTime(ftpFile.getTimestamp().getTimeInMillis());
                fileBuilder.setResourceSize(ftpFile.getSize());
                resourceBuilder.setFile(fileBuilder);
            }

            return resourceBuilder.build();

        } finally {
            FTPTransportUtil.disconnectFTP(ftpClient);
        }
    }


    @Override
    public Boolean isAvailable(String resourcePath) throws Exception{

        checkInitialized();

        FTPClient ftpClient = null;
        try {
            ftpClient = FTPTransportUtil.getFTPClient(ftpStorage, ftpSecret);
            FTPFile ftpFile = ftpClient.mlistFile(resourcePath);
            return ftpFile != null;
        } finally {
            FTPTransportUtil.disconnectFTP(ftpClient);
        }
    }
}

