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

package org.apache.airavata.mft.secret.server.backend.airavata;

import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.service.*;
import org.apache.airavata.model.credential.store.SSHCredential;

import java.util.Optional;

public class AiravataSecretBackend implements SecretBackend {

    @org.springframework.beans.factory.annotation.Value("${credential.server.host}")
    private String credentialServerHost;

    @org.springframework.beans.factory.annotation.Value("${credential.server.port}")
    private int credentialServerPort;

    @Override
    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception {
        CredentialStoreService.Client csClient = CredentialStoreClientFactory.createAiravataCSClient(credentialServerHost, credentialServerPort);
        String secretId = request.getSecretId();
        String[] parts = secretId.split(":");
        String csToken = parts[0];
        String user = parts[1];
        String gateway = parts[2];
        SSHCredential sshCredential = csClient.getSSHCredential(csToken, gateway);

        SCPSecret scpSecret = SCPSecret.newBuilder()
                .setPrivateKey(sshCredential.getPrivateKey())
                .setPublicKey(sshCredential.getPublicKey())
                .setPassphrase(sshCredential.getPassphrase())
                .setUser(user).build();

        return Optional.of(scpSecret);
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
