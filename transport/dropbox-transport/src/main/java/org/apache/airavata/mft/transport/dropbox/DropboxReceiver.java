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
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecret;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecretGetRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class DropboxReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(DropboxReceiver.class);

    private DbxClientV2 dbxClientV2;

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;

    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {

        this.resourceServiceHost = resourceServiceHost;
        this.resourceServicePort = resourceServicePort;
        this.secretServiceHost = secretServiceHost;
        this.secretServicePort = secretServicePort;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(AuthToken authToken, String resourceId, String credentialToken, ConnectorContext context) throws Exception {

        logger.info("Starting Dropbox Receiver stream for transfer {}", context.getTransferId());
        /*
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        GenericResource resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                .setResourceId(resourceId).build());

        if (resource.getStorageCase() != GenericResource.StorageCase.DROPBOXSTORAGE) {
            logger.error("Invalid storage type {} specified for resource {}", resource.getStorageCase(), resourceId);
            throw new Exception("Invalid storage type specified for resource " + resourceId);
        }

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        DropboxSecret dropboxSecret = secretClient.dropbox().getDropboxSecret(DropboxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        DbxRequestConfig config = DbxRequestConfig.newBuilder("mftdropbox/v1").build();
        dbxClientV2 = new DbxClientV2(config, dropboxSecret.getAccessToken());

        InputStream inputStream = dbxClientV2.files().download(resource.getFile().getResourcePath()).getInputStream();
        OutputStream os = context.getStreamBuffer().getOutputStream();
        int read;
        long bytes = 0;
        long fileSize = context.getMetadata().getResourceSize();
        byte[] buf = new byte[1024];
        while (true) {
            int bufSize = 0;

            if (buf.length < fileSize) {
                bufSize = buf.length;
            } else {
                bufSize = (int) fileSize;
            }
            bufSize = inputStream.read(buf, 0, bufSize);

            if (bufSize < 0) {
                break;
            }

            os.write(buf, 0, bufSize);
            os.flush();

            fileSize -= bufSize;
            if (fileSize == 0L)
                break;
        }

        os.close();

        logger.info("Completed Dropbox Receiver stream for transfer {}", context.getTransferId());


         */
    }

    @Override
    public void startStream(AuthToken authToken, String resourceId, String childResourcePath, String credentialToken,
                            ConnectorContext context) throws Exception {
        throw new UnsupportedOperationException();
    }
}
