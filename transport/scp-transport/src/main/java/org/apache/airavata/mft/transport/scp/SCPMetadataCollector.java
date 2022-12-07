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
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecret;
import org.apache.airavata.mft.resource.stubs.scp.storage.SCPStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SCPMetadataCollector implements MetadataCollector {

    private static final Logger logger = LoggerFactory.getLogger(SCPMetadataCollector.class);

    boolean initialized = false;
    private SCPStorage scpStorage;
    private SCPSecret scpSecret;

    @Override
    public void init(StorageWrapper storage, SecretWrapper secret) {
        this.scpStorage = storage.getScp();
        this.scpSecret = secret.getScp();
        this.initialized = true;
    }

    @Override
    public ResourceMetadata getResourceMetadata(String resourcePath) throws Exception {

        ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();
        try (SSHClient sshClient = getSSHClient(scpStorage, scpSecret)) {
            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {

                if (sftpClient.statExistence(resourcePath) == null) {
                    resourceBuilder.setError(MetadataFetchError.NOT_FOUND);
                    return resourceBuilder.build();
                }

                FileAttributes lstat = sftpClient.lstat(resourcePath);


                if (lstat.getType() == FileMode.Type.REGULAR) {
                    FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                    fileBuilder.setResourceSize(lstat.getSize());
                    fileBuilder.setCreatedTime(lstat.getAtime());
                    fileBuilder.setUpdateTime(lstat.getMtime());
                    fileBuilder.setFriendlyName(new File(resourcePath).getName());
                    fileBuilder.setResourcePath(resourcePath);
                    resourceBuilder.setFile(fileBuilder);
                } else if (lstat.getType() == FileMode.Type.DIRECTORY) {

                    DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();
                    dirBuilder.setResourcePath(resourcePath);
                    dirBuilder.setFriendlyName(new File(resourcePath).getName());
                    dirBuilder.setUpdateTime(lstat.getMtime());
                    dirBuilder.setCreatedTime(lstat.getAtime());

                    List<RemoteResourceInfo> dirList = sftpClient.ls(resourcePath);
                    for (RemoteResourceInfo rr: dirList) {
                        if (rr.isDirectory()) {
                            DirectoryMetadata.Builder subDir = DirectoryMetadata.newBuilder();
                            subDir.setResourcePath(rr.getPath());
                            subDir.setFriendlyName(rr.getName());
                            subDir.setCreatedTime(rr.getAttributes().getAtime());
                            subDir.setUpdateTime(rr.getAttributes().getMtime());
                            dirBuilder.addDirectories(subDir);
                        } else {
                            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                            fileBuilder.setResourceSize(rr.getAttributes().getSize());
                            fileBuilder.setCreatedTime(rr.getAttributes().getAtime());
                            fileBuilder.setUpdateTime(rr.getAttributes().getMtime());
                            fileBuilder.setFriendlyName(rr.getName());
                            fileBuilder.setResourcePath(rr.getPath());
                            dirBuilder.addFiles(fileBuilder);
                        }
                    }

                } else {
                    resourceBuilder.setError(MetadataFetchError.UNRECOGNIZED);
                }
            }
        }

        return resourceBuilder.build();
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("SCP Metadata Collector is not initialized");
        }
    }

    @Override
    public Boolean isAvailable(String resourcePath) throws Exception {

        checkInitialized();

        try (SSHClient sshClient = getSSHClient(scpStorage, scpSecret)) {
            logger.info("Checking the availability of file {}", resourcePath);
            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                return sftpClient.statExistence(resourcePath) != null;
            }
        }
    }

    private SSHClient getSSHClient(SCPStorage scpStorage, SCPSecret scpSecret) throws IOException {

        SSHClient sshClient = new SSHClient();

        sshClient.addHostKeyVerifier((h, p, key) -> true);

        File privateKeyFile = Files.createTempFile("id_rsa", "").toFile();
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

        sshClient.connect(scpStorage.getHost(), scpStorage.getPort());
        sshClient.auth(scpSecret.getUser(), am);

        return sshClient;
    }
}
