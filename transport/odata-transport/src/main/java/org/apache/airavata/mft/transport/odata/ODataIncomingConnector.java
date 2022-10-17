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

package org.apache.airavata.mft.transport.odata;

import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecret;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.GenericResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorage;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class ODataIncomingConnector implements IncomingStreamingConnector {

    private static final Logger logger = LoggerFactory.getLogger(ODataIncomingConnector.class);

    private CloseableHttpResponse response;
    CloseableHttpClient client;

    private String resourcePath;
    private ODataStorage odataStorage;

    @Override
    public void init(ConnectorConfig cc) throws Exception {
        try (StorageServiceClient storageServiceClient = StorageServiceClientBuilder
                .buildClient(cc.getResourceServiceHost(), cc.getResourceServicePort())) {

            odataStorage = storageServiceClient.odata()
                    .getODataStorage(ODataStorageGetRequest.newBuilder().setStorageId(cc.getStorageId()).build());
        }

        this.resourcePath = cc.getResourcePath();

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                cc.getSecretServiceHost(), cc.getSecretServicePort())) {

            ODataSecret oDataSecret = secretClient.odata().getODataSecret(ODataSecretGetRequest.newBuilder()
                    .setAuthzToken(cc.getAuthToken())
                    .setSecretId(cc.getCredentialToken()).build());

            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials
                    = new UsernamePasswordCredentials(oDataSecret.getUserName(), oDataSecret.getPassword());
            provider.setCredentials(AuthScope.ANY, credentials);

            client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        }
    }

    @Override
    public InputStream fetchInputStream() throws Exception {
        HttpGet httpGet = new HttpGet(odataStorage.getBaseUrl() +
                "/Products('" + resourcePath +"')/$value");
        response = client.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        logger.info("Received status code {} for resource path {}", statusCode, resourcePath);

        HttpEntity entity = response.getEntity();
        return entity.getContent();
    }

    @Override
    public void complete() throws Exception {
        if (response != null) {
            response.close();
        }

        if (client != null) {
            client.close();
        }
    }

    @Override
    public void failed() throws Exception {

    }
}
