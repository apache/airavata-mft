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
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecret;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecretGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropboxSender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(DropboxSender.class);

    private DbxClientV2 dbxClientV2;

    @Override
    public void init(AuthToken authZToken, String storageId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        DropboxSecret dropboxSecret = secretClient.dropbox().getDropboxSecret(DropboxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        DbxRequestConfig config = DbxRequestConfig.newBuilder("mftdropbox/v1").build();
        dbxClientV2 = new DbxClientV2(config, dropboxSecret.getAccessToken());
    }


    @Override
    public void destroy() {

    }

    @Override
    public void startStream(String targetPath, ConnectorContext context) throws Exception {
        logger.info("Starting Dropbox Sender stream for transfer {}", context.getTransferId());
        logger.info("Content length for transfer {} {}", context.getTransferId(), context.getMetadata().getResourceSize());

        FileMetadata metadata = dbxClientV2.files().uploadBuilder(targetPath)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(context.getStreamBuffer().getInputStream());
        logger.info("Completed Dropbox Sender stream for transfer {}", context.getTransferId());


    }
}
