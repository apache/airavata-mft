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

import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.credential.stubs.ftp.FTPSecret;
import org.apache.airavata.mft.credential.stubs.ftp.FTPSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.ftp.resource.FTPResource;
import org.apache.airavata.mft.resource.stubs.ftp.resource.FTPResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class FTPReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(FTPReceiver.class);

    private FTPResource resource;
    private boolean initialized;
    private FTPClient ftpClient;

    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {
        this.initialized = true;

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        this.resource = resourceClient.ftp().getFTPResource(FTPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        FTPSecret ftpSecret = secretClient.ftp().getFTPSecret(FTPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        this.ftpClient = FTPTransportUtil.getFTPClient(this.resource, ftpSecret);
    }

    @Override
    public void destroy() {
        FTPTransportUtil.disconnectFTP(ftpClient);
    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {

        if (ResourceTypes.FILE.equals(this.resource.getResourceCase().name())) {
            logger.info("Starting FTP receiver stream for transfer {}", context.getTransferId());

            checkInitialized();
            OutputStream streamOs = context.getStreamBuffer().getOutputStream();
            InputStream inputStream = ftpClient.retrieveFileStream(resource.getFile().getResourcePath());

            long fileSize = context.getMetadata().getResourceSize();

            byte[] buf = new byte[1024];
            while (true) {
                int bufSize;

                if (buf.length < fileSize) {
                    bufSize = buf.length;
                } else {
                    bufSize = (int) fileSize;
                }
                bufSize = inputStream.read(buf, 0, bufSize);

                if (bufSize < 0) {
                    break;
                }

                streamOs.write(buf, 0, bufSize);
                streamOs.flush();

                fileSize -= bufSize;
                if (fileSize == 0L)
                    break;
            }

            inputStream.close();
            streamOs.close();
            logger.info("Completed FTP receiver stream for transfer {}", context.getTransferId());

        } else {
            logger.error("Resource {} should be a FILE type. Found a {}",
                    this.resource.getResourceId(), this.resource.getResourceCase().name());
            throw new Exception("Resource " + this.resource.getResourceId() + " should be a FILE type. Found a " +
                    this.resource.getResourceCase().name());
        }
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("FTP Receiver is not initialized");
        }
    }
}
