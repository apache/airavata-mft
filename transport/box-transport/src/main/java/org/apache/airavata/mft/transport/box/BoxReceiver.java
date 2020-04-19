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
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.BoxResource;
import org.apache.airavata.mft.resource.service.BoxResourceGetRequest;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.BoxSecret;
import org.apache.airavata.mft.secret.service.BoxSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;


public class BoxReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(BoxReceiver.class);
    private BoxSecret boxSecret;
    private BoxResource boxResource;
    private BoxAPIConnection boxClient;


    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort,
                     String secretServiceHost, int secretServicePort) throws Exception {

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        boxResource = resourceClient.getBoxResource(BoxResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        boxSecret = secretClient.getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        boxClient = new BoxAPIConnection(boxSecret.getAccessToken());
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {

        logger.info("Starting Box Receiver stream for transfer {}", context.getTransferId());

        BoxFile file = new BoxFile(this.boxClient, this.boxResource.getBoxFileId());

        OutputStream os = context.getStreamBuffer().getOutputStream();
        file.download(os);
        os.flush();
        os.close();

        logger.info("Completed Box Receiver stream for transfer {}", context.getTransferId());
    }
}
