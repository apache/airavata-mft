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

import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.ftp.FTPSecret;
import org.apache.airavata.mft.credential.stubs.ftp.FTPSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
import org.apache.airavata.mft.resource.stubs.ftp.resource.FTPResource;
import org.apache.airavata.mft.resource.stubs.ftp.resource.FTPResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.ftp.storage.FTPStorage;
import org.apache.airavata.mft.resource.stubs.ftp.storage.FTPStorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class FTPMetadataCollector implements MetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(FTPMetadataCollector.class);

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    private boolean initialized = false;

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
            throw new IllegalStateException("FTP Metadata Collector is not initialized");
        }
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourceId, String credentialToken) {

        checkInitialized();
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        FTPResource ftpResource = resourceClient.ftp().getFTPResource(FTPResourceGetRequest.newBuilder().setResourceId(resourceId).build());
        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        FTPSecret ftpSecret = secretClient.ftp().getFTPSecret(FTPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        FileResourceMetadata resourceMetadata = new FileResourceMetadata();
        FTPClient ftpClient = null;
        try {
            ftpClient = FTPTransportUtil.getFTPClient(ftpResource.getFtpStorage(), ftpSecret);
            logger.info("Fetching metadata for resource {} in {}", ftpResource.getFile().getResourcePath(), ftpResource.getFtpStorage().getHost());

            FTPFile ftpFile = ftpClient.mlistFile(ftpResource.getFile().getResourcePath());

            if (ftpFile != null) {
                resourceMetadata.setResourceSize(ftpFile.getSize());
                resourceMetadata.setUpdateTime(ftpFile.getTimestamp().getTimeInMillis());
                if (ftpClient.hasFeature("MD5") && FTPReply.isPositiveCompletion(ftpClient.sendCommand("MD5 " + ftpResource.getFile().getResourcePath()))) {
                    String[] replies = ftpClient.getReplyStrings();
                    resourceMetadata.setMd5sum(replies[0]);
                } else {
                    logger.warn("MD5 fetch error out {}", ftpClient.getReplyString());
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch md5 for FTP resource {}", resourceId, e);
        } finally {
            FTPTransportUtil.disconnectFTP(ftpClient);
        }

        return resourceMetadata;
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
    public Boolean isAvailable(AuthToken authZToken, String resourceId, String credentialToken) {

        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        FTPResource ftpResource = resourceClient.ftp().getFTPResource(FTPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        return isAvailable(ftpResource, credentialToken);
    }

    @Override
    public Boolean isAvailable(AuthToken authToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {

        checkInitialized();

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        FTPStorage ftpStorage = resourceClient.ftp().getFTPStorage(FTPStorageGetRequest.newBuilder().setStorageId(parentResourceId).build());

        FTPResource ftpResource = FTPResource.newBuilder()
                .setFile(FileResource.newBuilder().setResourcePath(resourcePath).build())
                .setFtpStorage(ftpStorage).build();
        return isAvailable(ftpResource, credentialToken);
    }

    public Boolean isAvailable(FTPResource ftpResource, String credentialToken) {

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        FTPSecret ftpSecret = secretClient.ftp().getFTPSecret(FTPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        FTPClient ftpClient = null;
        try {
            ftpClient = FTPTransportUtil.getFTPClient(ftpResource.getFtpStorage(), ftpSecret);
            InputStream inputStream = null;

            switch (ftpResource.getResourceCase().name()) {
                case ResourceTypes.FILE:
                    inputStream = ftpClient.retrieveFileStream(ftpResource.getFile().getResourcePath());
                case ResourceTypes.DIRECTORY:
                    inputStream = ftpClient.retrieveFileStream(ftpResource.getDirectory().getResourcePath());
            }
            return !(inputStream == null || ftpClient.getReplyCode() == 550);
        } catch (Exception e) {
            logger.error("FTP client initialization failed ", e);
            return false;
        } finally {
            FTPTransportUtil.disconnectFTP(ftpClient);
        }
    }
}

