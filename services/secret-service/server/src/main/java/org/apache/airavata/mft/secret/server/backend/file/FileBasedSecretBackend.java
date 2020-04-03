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

            System.out.println("All resources ");
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
        return null;
    }

    @Override
    public boolean updateSCPSecret(SCPSecretUpdateRequest request) {
        return false;
    }

    @Override
    public boolean deleteSCPSecret(SCPSecretDeleteRequest request) {
        return false;
    }
}
