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

package org.apache.airavata.mft.transport.scp;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.SCPResource;
import org.apache.airavata.mft.resource.service.SCPResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.SCPSecret;
import org.apache.airavata.mft.secret.service.SCPSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SCPMetadataCollector implements MetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(SCPMetadataCollector.class);

    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    boolean initialized = false;

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
            throw new IllegalStateException("SCP Metadata Collector is not initialized");
        }
    }

    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws IOException {

        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        SCPResource scpResource = resourceClient.getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        try (SSHClient sshClient = getSSHClient(scpResource, scpSecret)) {

            logger.info("Fetching metadata for resource {} in {}", scpResource.getResourcePath(), scpResource.getScpStorage().getHost());

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                FileAttributes lstat = sftpClient.lstat(scpResource.getResourcePath());
                sftpClient.close();

                ResourceMetadata metadata = new ResourceMetadata();
                metadata.setResourceSize(lstat.getSize());
                metadata.setCreatedTime(lstat.getAtime());
                metadata.setUpdateTime(lstat.getMtime());

                try {
                    // TODO calculate md5 using the binary based on the OS platform. Eg: MacOS has md5. Linux has md5sum
                    // This only works for linux SCP resources. Improve to work in mac and windows resources
                    Session.Command md5Command = sshClient.startSession().exec("md5sum " + scpResource.getResourcePath());
                    StringWriter outWriter = new StringWriter();
                    StringWriter errorWriter = new StringWriter();

                    IOUtils.copy(md5Command.getInputStream(), outWriter, "UTF-8");
                    Integer exitStatus = md5Command.getExitStatus(); // get exit status ofter reading std out

                    if (exitStatus != null && exitStatus == 0) {
                        metadata.setMd5sum(outWriter.toString().split(" ")[0]);
                    } else {
                        IOUtils.copy(md5Command.getErrorStream(), errorWriter, "UTF-8");
                        logger.warn("MD5 fetch error out {}", errorWriter.toString());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to fetch md5 for SCP resource {}", resourceId, e);
                }

                return metadata;
            }
        }
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {

        checkInitialized();
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        SCPResource scpResource = resourceClient.getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        try (SSHClient sshClient = getSSHClient(scpResource, scpSecret)) {
            logger.info("Checking the availability of file {}", scpResource.getResourcePath());
            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                return sftpClient.statExistence(scpResource.getResourcePath()) != null;
            }
        }
    }

    private SSHClient getSSHClient(SCPResource scpResource, SCPSecret scpSecret) throws IOException {

        SSHClient sshClient = new SSHClient();

        sshClient.addHostKeyVerifier((h, p, key) -> true);

        File privateKeyFile = File.createTempFile("id_rsa", "");
        BufferedWriter writer = new BufferedWriter(new FileWriter(privateKeyFile));
        writer.write(scpSecret.getPrivateKey());
        writer.close();

        KeyProvider keyProvider = sshClient.loadKeys(privateKeyFile.getPath(), scpSecret.getPassphrase());
        final List<AuthMethod> am = new LinkedList<>();
        am.add(new AuthPublickey(keyProvider));
        am.add(new AuthKeyboardInteractive(new ChallengeResponseProvider() {
            @Override
            public List<String> getSubmethods() {
                return new ArrayList<>();
            }

            @Override
            public void init(Resource resource, String name, String instruction) {}

            @Override
            public char[] getResponse(String prompt, boolean echo) {
                return new char[0];
            }

            @Override
            public boolean shouldRetry() {
                return false;
            }
        }));

        sshClient.connect(scpResource.getScpStorage().getHost(), scpResource.getScpStorage().getPort());
        sshClient.auth(scpResource.getScpStorage().getUser(), am);

        return sshClient;
    }
}
