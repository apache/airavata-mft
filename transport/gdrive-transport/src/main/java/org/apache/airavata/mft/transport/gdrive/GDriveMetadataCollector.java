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

package org.apache.airavata.mft.transport.gdrive;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.GDriveResource;
import org.apache.airavata.mft.resource.service.GDriveResourceGetRequest;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.GDriveSecret;
import org.apache.airavata.mft.secret.service.GDriveSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

public class GDriveMetadataCollector implements MetadataCollector {

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    boolean initialized = false;
    // private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
    //       "https://www.googleapis.com/auth/drive.install");
    private static final Logger logger = LoggerFactory.getLogger(GDriveMetadataCollector.class);


    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) {
        this.resourceServiceHost = resourceServiceHost;
        this.resourceServicePort = resourceServicePort;
        this.secretServiceHost = secretServiceHost;
        this.secretServicePort = secretServicePort;
        this.initialized = true;
    }


    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("GDrive Metadata Collector is not initialized");
        }
    }

    @Override
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {
        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        GDriveResource gdriveResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gdriveSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());



        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString=gdriveSecret.getCredentialsJson();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), transport, jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes =  DriveScopes.all();
            //Arrays.asList(DriveScopes.DRIVE,"https://www.googleapis.com/auth/drive");
            credential = credential.createScoped(scopes);

        }


        Drive drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName("My Project").build();
        ResourceMetadata metadata = new ResourceMetadata();
        FileList fileList=drive.files().list().setFields("files(id,name,modifiedTime,md5Checksum,size,mimeType)").execute();

        for (File f:fileList.getFiles()){
            if(f.getName().equalsIgnoreCase(gdriveResource.getResourcePath())){
                metadata.setMd5sum(f.getMd5Checksum());
                metadata.setUpdateTime(f.getModifiedTime().getValue());
                metadata.setResourceSize(f.getSize().longValue());
            }
        }
        return metadata;
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {
        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        GDriveResource gdriveResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gdriveSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        logger.info("Inside GDRiveMetadata is available()");
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString=gdriveSecret.getCredentialsJson();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), transport, jsonFactory);
        if (credential.createScopedRequired()) {
            Collection<String> scopes =  DriveScopes.all();
            //Arrays.asList(DriveScopes.DRIVE,"https://www.googleapis.com/auth/drive");
            credential = credential.createScoped(scopes);

        }

        Drive drive = new Drive.Builder(transport, jsonFactory, credential)
                .setApplicationName("My Project").build();
        logger.info("Before getting resource");
        String id=null;

        FileList fileList=drive.files().list().setFields("files(id,name)").execute();
        for (File f:fileList.getFiles()) {
            if(f.getName().equalsIgnoreCase(gdriveResource.getResourcePath())){
                id = f.getId();
                return !drive.files().get(id).execute().isEmpty();
            }

        }

        return false;

    }
}
