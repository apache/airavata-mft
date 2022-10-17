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

import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecret;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecretGetRequest;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
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

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;

    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) {
        this.resourceServiceHost = resourceServiceHost;
        this.resourceServicePort = resourceServicePort;
        this.secretServiceHost = secretServiceHost;
        this.secretServicePort = secretServicePort;
    }

    private CloseableHttpClient getHttpClient(ODataSecret oDataSecret) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(oDataSecret.getUserName(), oDataSecret.getPassword());
        provider.setCredentials(AuthScope.ANY, credentials);

        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourcePath, String storageId,
                                                        String credentialToken) throws Exception {
        return findFileResourceMetadata(authZToken, resourcePath, storageId, credentialToken)
                .orElseThrow(() -> new Exception("Could not find a file resource entry for resource path " + resourcePath));
    }


    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String resourcePath,
                                                                  String storageId, String credentialToken) throws Exception {
        throw new UnsupportedOperationException("OData does not have directory structures");
    }

    @Override
    public Boolean isAvailable(AuthToken authZToken, String resourcePath, String storageId, String credentialToken) throws Exception {
        return findFileResourceMetadata(authZToken, resourcePath, storageId, credentialToken).isPresent();
    }

    private Optional<FileResourceMetadata> findFileResourceMetadata(AuthToken authZToken, String resourcePath,
                                                                    String storageId, String credentialToken) throws Exception {

        ODataStorage odataStorage;
        try (StorageServiceClient storageServiceClient = StorageServiceClientBuilder
                .buildClient(resourceServiceHost, resourceServicePort)) {

            odataStorage = storageServiceClient.odata()
                    .getODataStorage(ODataStorageGetRequest.newBuilder().setStorageId(storageId).build());
        }

        ODataSecret oDataSecret;
        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                secretServiceHost, secretServicePort)) {
            oDataSecret = secretClient.odata().getODataSecret(
                    ODataSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        }

        try (CloseableHttpClient httpClient = getHttpClient(oDataSecret)) {

            HttpGet httpGet = new HttpGet(odataStorage.getBaseUrl() +
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

    private Optional<FileResourceMetadata> parseXML(String xmlBody) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xmlBody)));
            doc.getDocumentElement().normalize();

            System.out.print("Root element: ");
            System.out.println(doc.getDocumentElement().getNodeName());
            NodeList properties = doc.getElementsByTagName("m:properties");

            if (properties.getLength() == 1) {

                FileResourceMetadata.Builder builder = FileResourceMetadata.Builder.newBuilder();

                Node propertyNode = properties.item(0);
                NodeList childNodes = propertyNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node item = childNodes.item(i);
                    switch (item.getNodeName()) {
                        case "d:ContentLength":
                            builder.withResourceSize(Long.parseLong(item.getTextContent()));
                            break;
                        case "d:CreationDate":
                            builder.withCreatedTime(Instant.parse(item.getTextContent() + "Z").toEpochMilli());
                            break;
                        case "d:ModificationDate":
                            builder.withUpdateTime(Instant.parse(item.getTextContent() + "Z").toEpochMilli());
                            break;
                        case "d:Name":
                            builder.withFriendlyName(item.getTextContent());
                            break;
                        case "d:Id":
                            builder.withResourcePath(item.getTextContent());
                            break;
                        case "d:Checksum":
                            NodeList checksumNodes = item.getChildNodes();
                            for (int j = 0; j < checksumNodes.getLength(); j++) {
                                Node item1 = checksumNodes.item(j);
                                if (item1.getNodeName().equals("d:Value")) {
                                    builder.withMd5sum(item1.getTextContent());
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
