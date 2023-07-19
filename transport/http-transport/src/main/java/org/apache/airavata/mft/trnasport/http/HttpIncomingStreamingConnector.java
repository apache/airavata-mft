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

package org.apache.airavata.mft.trnasport.http;

import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
import org.apache.airavata.mft.credential.stubs.http.HTTPSecret;
import org.apache.airavata.mft.resource.stubs.http.storage.HTTPStorage;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpIncomingStreamingConnector implements IncomingStreamingConnector {
    private static final Logger logger = LoggerFactory.getLogger(HttpIncomingStreamingConnector.class);

    private CloseableHttpClient httpClient;
    private HttpGet httpget;

    private ConnectorConfig connectorConfig;
    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.connectorConfig = connectorConfig;
        HTTPStorage httpStorage = connectorConfig.getStorage().getHttp();
        HTTPSecret httpSecret = connectorConfig.getSecret().getHttp();

        httpget = new HttpGet(httpStorage.getBaseUrl().endsWith("/")?
                httpStorage.getBaseUrl() + connectorConfig.getResourcePath() : httpStorage.getBaseUrl()
                + "/" +  connectorConfig.getResourcePath());

        switch (httpSecret.getAuthCase()) {
            case TOKEN:
                String token = httpSecret.getToken().getAccessToken();
                String authHeader = "Bearer " + token;
                httpget.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                break;
            case BASIC:
                String auth = httpSecret.getBasic().getUserName() + ":" + httpSecret.getBasic().getPassword();
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
                authHeader = "Basic " + new String(encodedAuth);
                httpget.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                break;
        }

        httpClient = HttpClientBuilder
                .create().build();
    }

    @Override
    public void complete() throws Exception {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    @Override
    public void failed() throws Exception {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    @Override
    public InputStream fetchInputStream() throws Exception {

        CloseableHttpResponse httpResponse = httpClient.execute(httpget);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            return new BufferedInputStream(httpResponse.getEntity().getContent());
        } else {
            logger.error("Invalid status code was received {} for for transfer path {}", statusCode,
                    connectorConfig.getStorage().getHttp().getBaseUrl() + "/" + connectorConfig.getResourcePath());
            throw new Exception("Invalid status code was received : " + statusCode);
        }
    }
}
