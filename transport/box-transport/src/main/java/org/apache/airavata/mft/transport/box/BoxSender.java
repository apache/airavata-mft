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
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxSender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(BoxSender.class);
    private BoxSecret boxSecret;
    private BoxResource boxResource;
    private BoxAPIConnection boxClient;


    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        boxResource = resourceClient.box().getBoxResource(BoxResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        boxSecret = secretClient.box().getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        boxClient = new BoxAPIConnection(boxSecret.getAccessToken());
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {

        logger.info("Starting Box Sender stream for transfer {}", context.getTransferId());
        logger.debug("Content length for transfer {} {}", context.getTransferId(), context.getMetadata().getResourceSize());

        if (ResourceTypes.FILE.equals(this.boxResource.getResourceCase().name())) {
            BoxFile file = new BoxFile(this.boxClient, this.boxResource.getFile().getResourcePath());

            // Upload chunks only if the file size is > 20mb
            // Ref: https://developer.box.com/guides/uploads/chunked/
            if (context.getMetadata().getResourceSize() > 20971520) {
                file.uploadLargeFile(context.getStreamBuffer().getInputStream(), context.getMetadata().getResourceSize());
            } else {
                file.uploadNewVersion(context.getStreamBuffer().getInputStream());
            }

            logger.info("Completed Box Sender stream for transfer {}", context.getTransferId());

        } else {
            logger.error("Resource {} should be a FILE type. Found a {}",
                    this.boxResource.getResourceId(), this.boxResource.getResourceCase().name());
            throw new Exception("Resource " + this.boxResource.getResourceId() + " should be a FILE type. Found a " +
                    this.boxResource.getResourceCase().name());
        }
    }
}
