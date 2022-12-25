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

import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecret;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorage;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.Instant;
import java.util.Optional;

public class ODataMetadataCollector implements MetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(ODataMetadataCollector.class);
    private ODataStorage oDataStorage;
    private ODataSecret oDataSecret;
    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        oDataStorage = storage.getOdata();
        oDataSecret = secret.getOdata();
    }

    private CloseableHttpClient getHttpClient(ODataSecret oDataSecret) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(oDataSecret.getUserName(), oDataSecret.getPassword());
        provider.setCredentials(AuthScope.ANY, credentials);

        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath, boolean recursiveSearch) throws Exception {
        ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();
        Optional<FileMetadata> fileResourceMetadata = findFileResourceMetadata(resourcePath);
        if (fileResourceMetadata.isPresent()) {
            resourceBuilder.setFile(fileResourceMetadata.get());
        } else {
            resourceBuilder.setError(MetadataFetchError.NOT_FOUND);
        }
        return resourceBuilder.build();
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {
        return findFileResourceMetadata(resourcePath).isPresent();
    }

    private Optional<FileMetadata> findFileResourceMetadata(String resourcePath) throws Exception {

        try (CloseableHttpClient httpClient = getHttpClient(oDataSecret)) {

            HttpGet httpGet = new HttpGet(oDataStorage.getBaseUrl() +
                    "/Products('" + resourcePath +"')");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    logger.error("Failed while invoking get product information endpoint. Got code {}", statusCode);
                    throw new Exception("Failed while invoking get product information endpoint. Got code " + statusCode);
                }
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                return parseXML(responseString);
            }
        }
    }

    private Optional<FileMetadata> parseXML(String xmlBody) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlBody)));
            doc.getDocumentElement().normalize();

            System.out.print("Root element: ");
            System.out.println(doc.getDocumentElement().getNodeName());
            NodeList properties = doc.getElementsByTagName("m:properties");

            if (properties.getLength() == 1) {

                FileMetadata.Builder builder = FileMetadata.newBuilder();

                Node propertyNode = properties.item(0);
                NodeList childNodes = propertyNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node item = childNodes.item(i);
                    switch (item.getNodeName()) {
                        case "d:ContentLength":
                            builder.setResourceSize(Long.parseLong(item.getTextContent()));
                            break;
                        case "d:CreationDate":
                            builder.setCreatedTime(Instant.parse(item.getTextContent() + "Z").toEpochMilli());
                            break;
                        case "d:ModificationDate":
                            builder.setUpdateTime(Instant.parse(item.getTextContent() + "Z").toEpochMilli());
                            break;
                        case "d:Name":
                            builder.setFriendlyName(item.getTextContent());
                            break;
                        case "d:Id":
                            builder.setResourcePath(item.getTextContent());
                            break;
                        case "d:Checksum":
                            NodeList checksumNodes = item.getChildNodes();
                            for (int j = 0; j < checksumNodes.getLength(); j++) {
                                Node item1 = checksumNodes.item(j);
                                if (item1.getNodeName().equals("d:Value")) {
                                    builder.setMd5Sum(item1.getTextContent());
                                }
                            }
                            break;
                    }
                }

                return Optional.of(builder.build());
            }

        } catch (Exception e) {
            logger.warn("Failed while parsing provided XML body", e);
        }

        return Optional.empty();
    }
}
