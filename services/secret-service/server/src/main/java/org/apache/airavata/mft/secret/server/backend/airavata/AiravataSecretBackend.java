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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AiravataSecretBackend implements SecretBackend {

    private static final Logger logger = LoggerFactory.getLogger(AiravataSecretBackend.class);

    @org.springframework.beans.factory.annotation.Value("${airavata.backend.credential.server.host}")
    private String credentialServerHost;

    @org.springframework.beans.factory.annotation.Value("${airavata.backend.credential.server.port}")
    private int credentialServerPort;

    @Override
    public void init() {
        logger.info("Initializing Airavata secret backend");
    }

    @Override
    public void destroy() {
        logger.info("Destroying Airavata secret backend");
    }

    @Override
    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception {
        CredentialStoreService.Client csClient = CredentialStoreClientFactory.createAiravataCSClient(credentialServerHost, credentialServerPort);
        String secretId = request.getSecretId();
        String[] parts = secretId.split(":");
        String csToken = parts[0];
        String gateway = parts[1];
        SSHCredential sshCredential = csClient.getSSHCredential(csToken, gateway);

        SCPSecret scpSecret = SCPSecret.newBuilder()
                .setPrivateKey(sshCredential.getPrivateKey())
                .setPublicKey(sshCredential.getPublicKey())
                .setPassphrase(sshCredential.getPassphrase()).build();

        return Optional.of(scpSecret);
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
        throw new UnsupportedOperationException("Operation is not supported in backend");
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
    public Optional<BoxSecret> getBoxSecret(BoxSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public BoxSecret createBoxSecret(BoxSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateBoxSecret(BoxSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteBoxSecret(BoxSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<AzureSecret> getAzureSecret(AzureSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
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
    public Optional<GCSSecret> getGCSSecret(GCSSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GCSSecret createGCSSecret(GCSSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSSecret(GCSSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSSecret(GCSSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<DropboxSecret> getDropboxSecret(DropboxSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public DropboxSecret createDropboxSecret(DropboxSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateDropboxSecret(DropboxSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteDropboxSecret(DropboxSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }


}
