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

import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.FTPResource;
import org.apache.airavata.mft.resource.service.FTPResourceGetRequest;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.FTPSecret;
import org.apache.airavata.mft.secret.service.FTPSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
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
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) {

        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        FTPResource ftpResource = resourceClient.getFTPResource(FTPResourceGetRequest.newBuilder().setResourceId(resourceId).build());
        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        FTPSecret ftpSecret = secretClient.getFTPSecret(FTPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        ResourceMetadata resourceMetadata = new ResourceMetadata();
        FTPClient ftpClient = null;
        try {
            ftpClient = FTPTransportUtil.getFTPClient(ftpResource, ftpSecret);
            logger.info("Fetching metadata for resource {} in {}", ftpResource.getResourcePath(), ftpResource.getFtpStorage().getHost());

            FTPFile ftpFile = ftpClient.mlistFile(ftpResource.getResourcePath());

            if (ftpFile != null) {
                resourceMetadata.setResourceSize(ftpFile.getSize());
                resourceMetadata.setUpdateTime(ftpFile.getTimestamp().getTimeInMillis());
                if (ftpClient.hasFeature("MD5") && FTPReply.isPositiveCompletion(ftpClient.sendCommand("MD5 " + ftpResource.getResourcePath()))) {
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
    public Boolean isAvailable(String resourceId, String credentialToken) {

        checkInitialized();

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        FTPResource ftpResource = resourceClient.getFTPResource(FTPResourceGetRequest.newBuilder().setResourceId(resourceId).build());
        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        FTPSecret ftpSecret = secretClient.getFTPSecret(FTPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        FTPClient ftpClient = null;
        try {
            ftpClient = FTPTransportUtil.getFTPClient(ftpResource, ftpSecret);
            InputStream inputStream = ftpClient.retrieveFileStream(ftpResource.getResourcePath());

            return !(inputStream == null || ftpClient.getReplyCode() == 550);
        } catch (Exception e) {
            logger.error("FTP client initialization failed ", e);
            return false;
        } finally {
            FTPTransportUtil.disconnectFTP(ftpClient);
        }
    }
}

