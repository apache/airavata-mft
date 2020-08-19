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
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;
import org.apache.airavata.mft.core.DirectoryResourceMetadata;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.ResourceTypes;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecret;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.DirectoryResource;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
import org.apache.airavata.mft.resource.stubs.scp.resource.SCPResource;
import org.apache.airavata.mft.resource.stubs.scp.resource.SCPResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
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

    private FileResourceMetadata getFileResourceMetadata(SCPResource scpResource, SCPSecret scpSecret, String parentResourceId) throws Exception {
        try (SSHClient sshClient = getSSHClient(scpResource, scpSecret)) {

            logger.info("Fetching metadata for resource {} in {}", scpResource.getFile().getResourcePath(), scpResource.getScpStorage().getHost());

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                FileAttributes lstat = sftpClient.lstat(scpResource.getFile().getResourcePath());
                sftpClient.close();

                FileResourceMetadata metadata = new FileResourceMetadata();
                metadata.setResourceSize(lstat.getSize());
                metadata.setCreatedTime(lstat.getAtime());
                metadata.setUpdateTime(lstat.getMtime());
                metadata.setParentResourceId(parentResourceId);
                metadata.setParentResourceType("SCP");
                metadata.setFriendlyName(new File(scpResource.getFile().getResourcePath()).getName());
                metadata.setResourcePath(scpResource.getFile().getResourcePath());

                try {
                    // TODO calculate md5 using the binary based on the OS platform. Eg: MacOS has md5. Linux has md5sum
                    // This only works for linux SCP resources. Improve to work in mac and windows resources
                    Session.Command md5Command = sshClient.startSession().exec("md5sum " + scpResource.getFile().getResourcePath());
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
                    logger.warn("Failed to fetch md5 for SCP resource {}", scpResource.getResourceId(), e);
                }
                return metadata;
            }
        }
    }

    public FileResourceMetadata getFileResourceMetadata(String resourceId, String credentialToken) throws Exception {

        checkInitialized();
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        SCPResource scpResource = resourceClient.scp().getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        return getFileResourceMetadata(scpResource, scpSecret, resourceId);
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        SCPResource parentSCPResource = resourceClient.scp().getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(parentResourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        validateParent(parentSCPResource, resourcePath);

        SCPResource scpResource = SCPResource.newBuilder()
                                        .setFile(FileResource.newBuilder()
                                        .setResourcePath(resourcePath).build())
                                        .setScpStorage(parentSCPResource.getScpStorage()).build();

        return getFileResourceMetadata(scpResource, scpSecret, parentResourceId);
    }

    private DirectoryResourceMetadata getDirectoryResourceMetadata(SCPResource scpResource, SCPSecret scpSecret, String parentResourceId) throws Exception {
        try (SSHClient sshClient = getSSHClient(scpResource, scpSecret)) {

            logger.info("Fetching metadata for resource {} in {}", scpResource.getFile().getResourcePath(), scpResource.getScpStorage().getHost());

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                List<RemoteResourceInfo> lsOut = sftpClient.ls(scpResource.getDirectory().getResourcePath());
                FileAttributes lsStat = sftpClient.lstat(scpResource.getDirectory().getResourcePath());
                sftpClient.close();

                DirectoryResourceMetadata.Builder dirMetadataBuilder = DirectoryResourceMetadata.Builder.getBuilder()
                                        .withLazyInitialized(false);

                for (RemoteResourceInfo rri : lsOut) {
                    if (rri.isDirectory()) {
                        DirectoryResourceMetadata.Builder childDirBuilder = DirectoryResourceMetadata.Builder.getBuilder()
                                        .withFriendlyName(rri.getName())
                                        .withResourcePath(rri.getPath())
                                        .withCreatedTime(rri.getAttributes().getAtime())
                                        .withUpdateTime(rri.getAttributes().getMtime())
                                        .withParentResourceId(parentResourceId)
                                        .withParentResourceType("SCP");
                        dirMetadataBuilder = dirMetadataBuilder.withDirectory(childDirBuilder.build());
                    }

                    if (rri.isRegularFile()) {
                        FileResourceMetadata.Builder childFileBuilder = FileResourceMetadata.Builder.getBuilder()
                                        .withFriendlyName(rri.getName())
                                        .withResourcePath(rri.getPath())
                                        .withCreatedTime(rri.getAttributes().getAtime())
                                        .withUpdateTime(rri.getAttributes().getMtime())
                                        .withParentResourceId(parentResourceId)
                                        .withParentResourceType("SCP");

                        dirMetadataBuilder = dirMetadataBuilder.withFile(childFileBuilder.build());
                    }
                }

                dirMetadataBuilder = dirMetadataBuilder.withFriendlyName(new File(scpResource.getDirectory().getResourcePath()).getName())
                        .withResourcePath(parentResourceId)
                        .withCreatedTime(lsStat.getAtime())
                        .withUpdateTime(lsStat.getMtime())
                        .withParentResourceId(parentResourceId)
                        .withParentResourceType("SCP");
                return dirMetadataBuilder.build();
            }
        }
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(String resourceId, String credentialToken) throws Exception {

        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        SCPResource scpPResource = resourceClient.scp().getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        return getDirectoryResourceMetadata(scpPResource, scpSecret, resourceId);
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        SCPResource parentSCPPResource = resourceClient.scp().getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(parentResourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        validateParent(parentSCPPResource, resourcePath);

        SCPResource scpResource = SCPResource.newBuilder().setScpStorage(parentSCPPResource.getScpStorage())
                        .setDirectory(DirectoryResource.newBuilder()
                        .setResourcePath(resourcePath).build()).build();

        return getDirectoryResourceMetadata(scpResource, scpSecret, parentResourceId);
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {

        checkInitialized();
        ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        SCPResource scpResource = resourceClient.scp().getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        try (SSHClient sshClient = getSSHClient(scpResource, scpSecret)) {
            logger.info("Checking the availability of file {}", scpResource.getFile().getResourcePath());
            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                switch (scpResource.getResourceCase().name()){
                    case ResourceTypes.FILE:
                        return sftpClient.statExistence(scpResource.getFile().getResourcePath()) != null;
                    case ResourceTypes.DIRECTORY:
                        return sftpClient.statExistence(scpResource.getDirectory().getResourcePath()) != null;
                }
                return false;
            }
        }
    }

    private void validateParent(SCPResource parentSCPResource, String resourcePath) throws Exception {
        if (!ResourceTypes.DIRECTORY.equals(parentSCPResource.getResourceCase().name())) {
            logger.error("Parent resource " + parentSCPResource.getResourceId() + " is not a DIRECTORY type");
            throw new Exception("Parent resource " + parentSCPResource.getResourceId() + " is not a DIRECTORY type");
        }

        String parentDir = parentSCPResource.getDirectory().getResourcePath();
        parentDir = parentDir.endsWith(File.separator) ? parentDir : parentDir + File.separator;
        if (!resourcePath.startsWith(parentDir)) {
            logger.error("Given resource path " + resourcePath + " is not a part of the parent resource path "
                    + parentSCPResource.getDirectory().getResourcePath());
            throw new Exception("Given resource path " + resourcePath + " is not a part of the parent resource path "
                    + parentSCPResource.getDirectory().getResourcePath());
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
