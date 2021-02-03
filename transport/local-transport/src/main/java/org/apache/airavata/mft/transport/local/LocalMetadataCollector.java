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

import org.apache.airavata.mft.core.AuthZToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.local.resource.LocalResource;
import org.apache.airavata.mft.resource.stubs.local.resource.LocalResourceGetRequest;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

public class LocalMetadataCollector implements MetadataCollector {

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
            throw new IllegalStateException("Local Metadata Collector is not initialized");
        }
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthZToken authZToken, String resourceId, String credentialToken) throws Exception {

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        LocalResource localResource = resourceClient.local().getLocalResource(LocalResourceGetRequest.newBuilder().setResourceId(resourceId).build());
        File resourceFile = new File(localResource.getFile().getResourcePath());
        if (resourceFile.exists()) {

            BasicFileAttributes basicFileAttributes = Files.readAttributes(Path.of(localResource.getFile().getResourcePath()), BasicFileAttributes.class);
            FileResourceMetadata metadata = new FileResourceMetadata();
            metadata.setCreatedTime(basicFileAttributes.creationTime().toMillis());
            metadata.setUpdateTime(basicFileAttributes.lastModifiedTime().toMillis());
            metadata.setResourceSize(basicFileAttributes.size());

            MessageDigest digest = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(localResource.getFile().getResourcePath());
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            };
            fis.close();
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            metadata.setMd5sum(sb.toString());

            return metadata;
        } else {
            throw new Exception("Resource with id " + resourceId + " in path " + localResource.getFile().getResourcePath() + " does not exist");
        }
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
       ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        LocalResource localResource = resourceClient.local().getLocalResource(LocalResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        switch (localResource.getResourceCase().name()){
            case ResourceTypes.FILE:
                return new File(localResource.getFile().getResourcePath()).exists();
            case ResourceTypes.DIRECTORY:
                return new File(localResource.getDirectory().getResourcePath()).exists();
        }
        return false;
    }
}
