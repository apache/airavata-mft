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
import org.apache.airavata.mft.common.AuthToken;
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
import org.apache.airavata.mft.resource.stubs.common.GenericResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResourceGetRequest;
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

    private FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, GenericResource scpResource, SCPSecret scpSecret) throws Exception {
        try (SSHClient sshClient = getSSHClient(scpResource, scpSecret)) {

            logger.info("Fetching metadata for resource {} in {}", scpResource.getFile().getResourcePath(), scpResource.getScpStorage().getHost());

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                FileAttributes lstat = sftpClient.lstat(scpResource.getFile().getResourcePath());
                sftpClient.close();

                FileResourceMetadata metadata = new FileResourceMetadata();
                metadata.setResourceSize(lstat.getSize());
                metadata.setCreatedTime(lstat.getAtime());
                metadata.setUpdateTime(lstat.getMtime());
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

    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {

        checkInitialized();

        GenericResource scpResource;
        SCPSecret scpSecret;
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort)) {
            scpResource = resourceClient.get()
                    .getGenericResource(GenericResourceGetRequest.newBuilder()
                            .setAuthzToken(authZToken).setResourceId(resourceId).build());
        }

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort)) {
            scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                    .setAuthzToken(authZToken).setSecretId(credentialToken).build());
        }

        return getFileResourceMetadata(authZToken,scpResource, scpSecret);
    }

    @Override
    public FileResourceMetadata getFileResourceMetadata(AuthToken authZToken, String parentResourceId, String childResourcePath, String credentialToken) throws Exception {

        GenericResource resource;
        SCPSecret scpSecret;
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort)) {
            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(authZToken)
                    .setResourceId(parentResourceId).build());
        }

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort)) {
            scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                    .setAuthzToken(authZToken).setSecretId(credentialToken).build());
        }

        boolean isChildPath = false;
        if (childResourcePath != null && !"".equals(childResourcePath)) {
            isChildPath = true;
        }

        String resourcePath = null;
        switch (resource.getResourceCase()) {
            case FILE:
                if (isChildPath) {
                    throw new Exception("A child path can not be associated with a file parent");
                }
                resourcePath = resource.getFile().getResourcePath();
                break;
            case DIRECTORY:
                resourcePath = resource.getDirectory().getResourcePath();
                if (isChildPath) {
                    if (!childResourcePath.startsWith(resourcePath)) {
                        throw new Exception("Child path " + childResourcePath + " is not in the parent path " + resourcePath);
                    }
                    resourcePath = childResourcePath;
                }

                break;
            case RESOURCE_NOT_SET:
                throw new Exception("Resource was not set in resource with id " + parentResourceId);
        }

        GenericResource scpResource2 = GenericResource.newBuilder()
                .setFile(FileResource.newBuilder()
                        .setResourcePath(resourcePath).build())
                .setScpStorage(resource.getScpStorage()).build();

        return getFileResourceMetadata(authZToken, scpResource2, scpSecret);
    }

    private DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, GenericResource scpResource, SCPSecret scpSecret) throws Exception {
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
                                        .withUpdateTime(rri.getAttributes().getMtime());
                        dirMetadataBuilder = dirMetadataBuilder.withDirectory(childDirBuilder.build());
                    }

                    if (rri.isRegularFile()) {
                        FileResourceMetadata.Builder childFileBuilder = FileResourceMetadata.Builder.newBuilder()
                                        .withFriendlyName(rri.getName())
                                        .withResourcePath(rri.getPath())
                                        .withCreatedTime(rri.getAttributes().getAtime())
                                        .withUpdateTime(rri.getAttributes().getMtime())
                                        .withResourceSize(rri.getAttributes().getSize());

                        dirMetadataBuilder = dirMetadataBuilder.withFile(childFileBuilder.build());
                    }
                }

                dirMetadataBuilder = dirMetadataBuilder.withFriendlyName(new File(scpResource.getDirectory().getResourcePath()).getName())
                        .withResourcePath(scpResource.getDirectory().getResourcePath())
                        .withCreatedTime(lsStat.getAtime())
                        .withUpdateTime(lsStat.getMtime());
                return dirMetadataBuilder.build();
            }
        }
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {

        GenericResource resource;
        SCPSecret scpSecret;
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort)) {
            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(authZToken)
                    .setResourceId(resourceId).build());
        }

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort)) {
            scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                    .setAuthzToken(authZToken).setSecretId(credentialToken).build());
        }

        return getDirectoryResourceMetadata(authZToken, resource, scpSecret);
    }

    @Override
    public DirectoryResourceMetadata getDirectoryResourceMetadata(AuthToken authZToken, String parentResourceId, String childResourcePath, String credentialToken) throws Exception {

        GenericResource resource;
        SCPSecret scpSecret;
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort)) {
            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(authZToken).setResourceId(parentResourceId).build());
        }

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort)) {
            scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                    .setAuthzToken(authZToken).setSecretId(credentialToken).build());
        }

        boolean isChildPath = false;
        if (childResourcePath != null && !"".equals(childResourcePath)) {
            isChildPath = true;
        }

        String resourcePath = null;

        switch (resource.getResourceCase()){
            case FILE:
                if (isChildPath){
                    throw new Exception("A child path can not be associated with a file parent");
                }
                resourcePath = resource.getFile().getResourcePath();
                break;
            case DIRECTORY:
                resourcePath = resource.getDirectory().getResourcePath();
                if (isChildPath) {
                    if (!childResourcePath.startsWith(resourcePath)) {
                        throw new Exception("Child path " + childResourcePath + " is not in the parent path " + resourcePath);
                    }
                    resourcePath = childResourcePath;
                }

                break;
            case RESOURCE_NOT_SET:
                throw new Exception("Resource was not set in resource with id " + parentResourceId);
        }

        GenericResource scpResource = GenericResource.newBuilder()
                .setDirectory(DirectoryResource.newBuilder().setResourcePath(resourcePath).build())
                .setScpStorage(resource.getScpStorage()).build();

        return getDirectoryResourceMetadata(authZToken,scpResource, scpSecret);
    }

    @Override
    public Boolean isAvailable(AuthToken authZToken, String resourceId, String credentialToken) throws Exception {

        checkInitialized();

        GenericResource resource;
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort)) {
            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(authZToken).setResourceId(resourceId).build());
        }

        return isAvailable(authZToken, resource, credentialToken);
    }

    @Override
    public Boolean isAvailable(AuthToken authToken, String parentResourceId, String resourcePath, String credentialToken) throws Exception {
        checkInitialized();

        GenericResource resource;
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort)) {
            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(authToken).setResourceId(parentResourceId).build());
        }

        validateParent(resource, resourcePath);

        GenericResource targetScpResource = GenericResource.newBuilder()
                .setFile(FileResource.newBuilder().setResourcePath(resourcePath).build())
                .setScpStorage(resource.getScpStorage()).build();

        return isAvailable(authToken, targetScpResource, credentialToken);
    }

    public Boolean isAvailable(AuthToken authToken, GenericResource scpResource, String credentialToken) throws Exception {


        SCPSecret scpSecret;

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort)) {
            scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                    .setAuthzToken(authToken).setSecretId(credentialToken).build());
        }

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

    private void validateParent(GenericResource parentSCPResource, String resourcePath) throws Exception {
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

    private SSHClient getSSHClient(GenericResource scpResource, SCPSecret scpSecret) throws IOException {

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
        sshClient.auth(scpSecret.getUser(), am);

        return sshClient;
    }
}
