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


import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.credential.stubs.box.BoxSecret;
import org.apache.airavata.mft.credential.stubs.box.BoxSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.box.resource.BoxResource;
import org.apache.airavata.mft.resource.stubs.box.resource.BoxResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.box.storage.BoxStorage;
import org.apache.airavata.mft.resource.stubs.box.storage.BoxStorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;


public class BoxReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(BoxReceiver.class);
    private BoxAPIConnection boxClient;

    @Override
    public void init(String storageId, String credentialToken, String resourceServiceHost, int resourceServicePort,
                     String secretServiceHost, int secretServicePort) throws Exception {

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        BoxSecret boxSecret = secretClient.box().getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        boxClient = new BoxAPIConnection(boxSecret.getAccessToken());
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(String targetPath, ConnectorContext context) throws Exception {

        logger.info("Starting Box Receiver stream for transfer {}", context.getTransferId());

        BoxFile file = new BoxFile(this.boxClient, targetPath);

        OutputStream os = context.getStreamBuffer().getOutputStream();
        file.download(os);
        os.flush();
        os.close();

        logger.info("Completed Box Receiver stream for transfer {}", context.getTransferId());
    }
}
