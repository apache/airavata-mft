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

import org.apache.airavata.mft.agent.stub.FileMetadata;
import org.apache.airavata.mft.agent.stub.ResourceMetadata;
import org.apache.airavata.mft.agent.stub.SecretWrapper;
import org.apache.airavata.mft.agent.stub.StorageWrapper;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.http.HTTPSecret;
import org.apache.airavata.mft.resource.stubs.http.storage.HTTPStorage;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.nio.charset.StandardCharsets;

public class HttpMetadataCollector implements MetadataCollector {

    private HTTPStorage httpStorage;
    private HTTPSecret httpSecret;
    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        httpStorage = storage.getHttp();
        httpSecret = secret.getHttp();
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {
        HttpGet httpget = new HttpGet(httpStorage.getBaseUrl().endsWith("/")?
                httpStorage.getBaseUrl() + resourcePath : httpStorage.getBaseUrl()
                + "/" +  resourcePath);

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

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpget)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    return ResourceMetadata.newBuilder().setFile(
                            FileMetadata.newBuilder()
                                    .setResourcePath(resourcePath)
                                    .setResourceSize(httpResponse.getEntity().getContentLength())
                                    .setFriendlyName(resourcePath).build()).build();
                } else {
                    throw new Exception("Status code came as " + statusCode + " for resource " + resourcePath);
                }
            }
        }

    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {
        return true; // It is not usable to check if the path is available
    }
}
