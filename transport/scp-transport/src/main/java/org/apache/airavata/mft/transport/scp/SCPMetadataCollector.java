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
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SCPMetadataCollector implements MetadataCollector {

    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws IOException {

        SSHResourceIdentifier sshResourceIdentifier = SCPTransportUtil.getSSHResourceIdentifier(resourceId);

        try (SSHClient sshClient = new SSHClient()) {
            sshClient.addHostKeyVerifier((h, p, key) -> true);
            KeyProvider keyProvider = sshClient.loadKeys(sshResourceIdentifier.getKeyFile(), sshResourceIdentifier.getKeyPassphrase());
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

            sshClient.connect(sshResourceIdentifier.getHost(), sshResourceIdentifier.getPort());
            sshClient.auth(sshResourceIdentifier.getUser(), am);

            try (SFTPClient sftpClient = sshClient.newSFTPClient()) {
                FileAttributes lstat = sftpClient.lstat(sshResourceIdentifier.getRemotePath());
                sftpClient.close();

                ResourceMetadata metadata = new ResourceMetadata();
                metadata.setResourceSize(lstat.getSize());
                metadata.setCreatedTime(lstat.getAtime());
                metadata.setUpdateTime(lstat.getMtime());
                return metadata;
            }
        }
    }
}
