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

package org.apache.airavata.mft.transport.gcp;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.gcs.resource.GCSResource;
import org.apache.airavata.mft.resource.stubs.gcs.resource.GCSResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;


public class GCSReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(GCSReceiver.class);

    private GCSResource gcsResource;
    private Storage storage;

    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        this.gcsResource = resourceClient.gcs().getGCSResource(GCSResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        GCSSecret gcsSecret = secretClient.gcs().getGCSSecret(GCSSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString = gcsSecret.getCredentialsJson();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)));
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }
        storage = new Storage.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        logger.info("Starting GCS Receiver stream for transfer {}", context.getTransferId());

        if (ResourceTypes.FILE.equals(this.gcsResource.getResourceCase().name())) {
            InputStream inputStream = storage.objects().get(this.gcsResource.getGcsStorage().getBucketName(),
                                        this.gcsResource.getFile().getResourcePath()).executeMediaAsInputStream();
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

            logger.info("Completed GCS Receiver stream for transfer {}", context.getTransferId());

        } else {
            logger.error("Resource {} should be a FILE type. Found a {}",
                    this.gcsResource.getResourceId(), this.gcsResource.getResourceCase().name());
            throw new Exception("Resource " + this.gcsResource.getResourceId() + " should be a FILE type. Found a " +
                    this.gcsResource.getResourceCase().name());
        }
    }
}
