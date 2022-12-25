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

package org.apache.airavata.mft.transport.local;

import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.stubs.local.storage.LocalStorage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class LocalMetadataCollector implements MetadataCollector {

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    boolean initialized = false;

    private LocalStorage localStorage;
    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.localStorage = storage.getLocal();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Local Metadata Collector is not initialized");
        }
    }

    private FileMetadata.Builder getFileBuilderFromPath(File file) throws Exception {

        BasicFileAttributes basicFileAttributes = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);
        FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
        fileBuilder.setCreatedTime(basicFileAttributes.creationTime().toMillis());
        fileBuilder.setUpdateTime(basicFileAttributes.lastModifiedTime().toMillis());
        fileBuilder.setResourceSize(basicFileAttributes.size());

        fileBuilder.setResourcePath(file.getPath());
        fileBuilder.setFriendlyName(file.getName());

        return fileBuilder;
    }
    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {

        checkInitialized();

        ResourceMetadata.Builder resouceBuilder = ResourceMetadata.newBuilder();
        File resourceFile = new File(resourcePath);
        if (resourceFile.exists()) {

            if (resourceFile.isFile()) {
                FileMetadata.Builder fileBuilder = getFileBuilderFromPath(resourceFile);
                resouceBuilder.setFile(fileBuilder);

            } else if (resourceFile.isDirectory()) {
                DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();
                BasicFileAttributes basicFileAttributes = Files.readAttributes(Path.of(resourcePath), BasicFileAttributes.class);
                dirBuilder.setCreatedTime(basicFileAttributes.creationTime().toMillis());
                dirBuilder.setUpdateTime(basicFileAttributes.lastModifiedTime().toMillis());

                Stream<Path> fileList = Files.list(resourceFile.toPath());
                fileList.forEach(p -> {
                    try {
                        if (p.toFile().isFile()) {

                            FileMetadata.Builder fileBuilder = getFileBuilderFromPath(resourceFile);
                            dirBuilder.addFiles(fileBuilder);
                        } else if (p.toFile().isDirectory()) {
                            DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                            BasicFileAttributes bfa = Files.readAttributes(p, BasicFileAttributes.class);
                            subDirBuilder.setCreatedTime(bfa.creationTime().toMillis());
                            subDirBuilder.setUpdateTime(bfa.lastModifiedTime().toMillis());
                            dirBuilder.addDirectories(subDirBuilder);
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                });

                resouceBuilder.setDirectory(dirBuilder);
            } else {
                resouceBuilder.setError(MetadataFetchError.UNRECOGNIZED);
            }
        } else {
            resouceBuilder.setError(MetadataFetchError.NOT_FOUND);
        }

        return resouceBuilder.build();
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {

        checkInitialized();
        return new File(resourcePath).exists();
    }
}
