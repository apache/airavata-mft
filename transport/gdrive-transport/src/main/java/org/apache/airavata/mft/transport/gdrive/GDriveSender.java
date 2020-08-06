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
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.GDriveResource;
import org.apache.airavata.mft.resource.service.GDriveResourceGetRequest;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.GDriveSecret;
import org.apache.airavata.mft.secret.service.GDriveSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;


public class GDriveSender implements Connector {
    private static final Logger logger = LoggerFactory.getLogger(GDriveSender.class);

    private GDriveResource gdriveResource;
    private Drive drive;
    private JsonObject jsonObject;


    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        this.gdriveResource = resourceClient.getGDriveResource(GDriveResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GDriveSecret gdriveSecret = secretClient.getGDriveSecret(GDriveSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        String jsonString = gdriveSecret.getCredentialsJson();
        jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
        GoogleCredential credential = GoogleCredential.fromStream(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), transport, jsonFactory);

        if (credential.createScopedRequired()) {
            Collection<String> scopes = DriveScopes.all();
            credential = credential.createScoped(scopes);
        }

        drive = new Drive.Builder(transport, jsonFactory, credential).build();

    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        logger.info("Starting GDrive send for remote server for transfer {} ", context.getTransferId());
        String id = null;
        boolean fileupdated = false;
        String entityUser = jsonObject.get("client_email").getAsString();
        File fileMetadata = new File();

        InputStreamContent contentStream = new InputStreamContent("", context.getStreamBuffer().getInputStream());

        fileMetadata.setName(this.gdriveResource.getResourcePath());
        FileList fileList = drive.files().list()
                .setQ("name = '" + gdriveResource.getResourcePath() + "'")
                .setFields("files(id,name)")
                .execute();

        List<File> filtered = fileList.getFiles();
        if (filtered == null || filtered.size() == 0) {
            File file = drive.files().create(fileMetadata, contentStream).setFields("id").execute();
            Permission userPermission = new Permission();
            userPermission.setType("user").setRole("writer").setEmailAddress(entityUser);
            drive.permissions().create(file.getId(), userPermission).execute();
        } else if (filtered.size() > 1) {
            throw new Exception("More than one file found with name " + gdriveResource.getResourcePath());
        } else {
            id = filtered.get(0).getId();
            File file = drive.files().get(id).execute();
            drive.files().update(file.getId(), fileMetadata, contentStream).setFields("id").execute();
        }

        logger.info("Completed GDrive send for remote server for transfer {} ", context.getTransferId());
    }
}