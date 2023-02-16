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

import com.google.common.collect.FluentIterable;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Container;
import org.jclouds.openstack.swift.v1.domain.ObjectList;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.ObjectApi;

import java.util.Properties;

public class SwiftMetadataCollector implements MetadataCollector {
    boolean initialized = false;
    private SwiftStorage swiftStorage;
    private SwiftSecret swiftSecret;

    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.swiftStorage = storage.getSwift();
        this.swiftSecret = secret.getSwift();
        this.initialized = true;
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Swift Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {
        checkInitialized();

        SwiftApi swiftApi = SwiftUtil.createSwiftApi(swiftSecret, swiftStorage);

        try {
            ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();
            if ("".equals(resourcePath) && "".equals(swiftStorage.getContainer())) {
                FluentIterable<Container> containers = swiftApi.getContainerApi(swiftStorage.getRegion()).list();
                DirectoryMetadata.Builder parentDir = DirectoryMetadata.newBuilder();
                parentDir.setResourcePath("");
                parentDir.setFriendlyName("");
                containers.forEach(container -> {
                    DirectoryMetadata.Builder bucketDir = DirectoryMetadata.newBuilder();
                    bucketDir.setFriendlyName(container.getName());
                    bucketDir.setResourcePath(container.getName());
                    parentDir.addDirectories(bucketDir);
                });
                resourceBuilder.setDirectory(parentDir);

            } else {
                ObjectApi objectApi = swiftApi.getObjectApi(swiftStorage.getRegion(), swiftStorage.getContainer());
                if ("".equals(resourcePath)) {

                    DirectoryMetadata.Builder rootDirBuilder = DirectoryMetadata.newBuilder();

                    ObjectList objectList = objectApi.list();
                    objectList.forEach(swiftObject -> {
                        FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                        fileBuilder.setFriendlyName(swiftObject.getName());
                        fileBuilder.setResourcePath(swiftObject.getName());
                        fileBuilder.setCreatedTime(swiftObject.getLastModified().getTime());
                        fileBuilder.setUpdateTime(swiftObject.getLastModified().getTime());
                        fileBuilder.setResourceSize(swiftObject.getPayload().getContentMetadata().getContentLength());
                        rootDirBuilder.addFiles(fileBuilder);
                    });
                    resourceBuilder.setDirectory(rootDirBuilder);
                } else {
                    SwiftObject swiftObject = objectApi.get(resourcePath);

                    if (swiftObject == null) {
                        resourceBuilder.setError(MetadataFetchError.NOT_FOUND);
                        return resourceBuilder.build();
                    }

                    FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                    fileBuilder.setFriendlyName(swiftObject.getName());
                    fileBuilder.setResourcePath(swiftObject.getName());
                    fileBuilder.setCreatedTime(swiftObject.getLastModified().getTime());
                    fileBuilder.setUpdateTime(swiftObject.getLastModified().getTime());
                    fileBuilder.setResourceSize(swiftObject.getPayload().getContentMetadata().getContentLength());
                    resourceBuilder.setFile(fileBuilder);
                }
            }
            return resourceBuilder.build();
        } finally{
            swiftApi.close();
        }
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {
        checkInitialized();

        SwiftApi swiftApi = SwiftUtil.createSwiftApi(swiftSecret, swiftStorage);

        try {
            ObjectApi objectApi = swiftApi.getObjectApi(swiftStorage.getRegion(), swiftStorage.getContainer());

            SwiftObject swiftObject = objectApi.get(resourcePath);

            return swiftObject != null;
        } finally {
            swiftApi.close();
        }
    }
}
