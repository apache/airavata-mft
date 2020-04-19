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

package org.apache.airavata.mft.secret.server.backend.file;

import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.service.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileBasedSecretBackend implements SecretBackend {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedSecretBackend.class);

    @org.springframework.beans.factory.annotation.Value("${file.backend.secret.file}")
    private String secretFile;

    @Override
    public void init() {
        logger.info("Initializing file based secret backend");
    }

    @Override
    public void destroy() {
        logger.info("Destroying file based secret backend");
    }

    @Override
    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedSecretBackend.class.getClassLoader().getResourceAsStream(secretFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<SCPSecret> scpSecrets = (List<SCPSecret>) resourceList.stream()
                    .filter(resource -> "SCP".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        SCPSecret scpSecret = SCPSecret.newBuilder()
                                .setSecretId(r.get("secretId").toString())
                                .setPublicKey(r.get("publicKey").toString())
                                .setPassphrase(r.get("passphrase").toString())
                                .setPrivateKey(r.get("privateKey").toString()).build();

                        return scpSecret;
                    }).collect(Collectors.toList());
            return scpSecrets.stream().filter(r -> request.getSecretId().equals(r.getSecretId())).findFirst();
        }
    }

    @Override
    public SCPSecret createSCPSecret(SCPSecretCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateSCPSecret(SCPSecretUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteSCPSecret(SCPSecretDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<S3Secret> getS3Secret(S3SecretGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedSecretBackend.class.getClassLoader().getResourceAsStream(secretFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<S3Secret> scpSecrets = (List<S3Secret>) resourceList.stream()
                    .filter(resource -> "S3".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        S3Secret s3Secret = S3Secret.newBuilder()
                                .setSecretId(r.get("secretId").toString())
                                .setAccessKey(r.get("accessKey").toString())
                                .setSecretKey(r.get("secretKey").toString()).build();

                        return s3Secret;
                    }).collect(Collectors.toList());
            return scpSecrets.stream().filter(r -> request.getSecretId().equals(r.getSecretId())).findFirst();
        }
    }

    @Override
    public S3Secret createS3Secret(S3SecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateS3Secret(S3SecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteS3Secret(S3SecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<AzureSecret> getAzureSecret(AzureSecretGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedSecretBackend.class.getClassLoader().getResourceAsStream(secretFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<AzureSecret> azureSecrets = (List<AzureSecret>) resourceList.stream()
                    .filter(resource -> "AZURE".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        AzureSecret azureSecret = AzureSecret.newBuilder()
                                .setSecretId(r.get("secretId").toString())
                                .setConnectionString(r.get("connectionString").toString()).build();

                        return azureSecret;
                    }).collect(Collectors.toList());
            return azureSecrets.stream().filter(r -> request.getSecretId().equals(r.getSecretId())).findFirst();
        }
    }

    @Override
    public AzureSecret createAzureSecret(AzureSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateAzureSecret(AzureSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteAzureSecret(AzureSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<GCSSecret> getGCSSecret(AzureSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GCSSecret createGCSSecret(AzureSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSSecret(AzureSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSSecret(AzureSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }
}
