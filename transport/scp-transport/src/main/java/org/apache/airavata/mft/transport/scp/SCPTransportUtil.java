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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.*;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.SCPSecret;
import org.apache.airavata.mft.secret.service.SCPSecretCreateRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;

import java.util.Properties;

public class SCPTransportUtil {
    // TODO replace with an API call to the registry


    public static void main(String args[]) {
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient("localhost", 7002);
        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient("localhost", 7003);

        //resourceClient.getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId("82848d70-ef71-47d2-935a-4fd3e0184a42").build());
        SCPStorage scpStorage1 = resourceClient.createSCPStorage(SCPStorageCreateRequest.newBuilder().setPort(22).setHost("pgadev.scigap.org").build());
        SCPResource scpResource1 = resourceClient.createSCPResource(SCPResourceCreateRequest.newBuilder().setScpStorageId(scpStorage1.getStorageId())
                .setResourcePath("/var/www/portals/gateway-user-data/dev-seagrid/eromads6/DefaultProject/Gaussian_C11470169729/2mb.txt").build());
        SCPResource scpResource2 = resourceClient.createSCPResource(SCPResourceCreateRequest.newBuilder().setScpStorageId(scpStorage1.getStorageId())
                .setResourcePath("/var/www/portals/gateway-user-data/dev-seagrid/eromads6/DefaultProject/Gaussian_C11470169729/new-file.txt").build());

        System.out.println("Resource 1 id " + scpResource1.getResourceId());
        System.out.println("Resource 2 id " + scpResource2.getResourceId());

        SCPSecret scpSecret1 = secretClient.createSCPSecret(SCPSecretCreateRequest.newBuilder()
                .setUser("pga")
                .setPassphrase("shithappens@12")
                .setPrivateKey("/Users/dimuthu/.ssh/id_rsa").build());

        System.out.println("Secret id " + scpSecret1.getSecretId());
    }

    public static SSHResourceIdentifier getSSHResourceIdentifier(String resourceId) {

        SSHResourceIdentifier identifier = new SSHResourceIdentifier();
        switch (resourceId){
            case "1":
                identifier.setHost("pgadev.scigap.org");
                identifier.setUser("pga");
                identifier.setPort(22);
                identifier.setKeyFile("/Users/dwannipu/.ssh/id_rsa");
                identifier.setKeyPassphrase(null);
                identifier.setRemotePath("/var/www/portals/gateway-user-data/dev-seagrid/eromads6/DefaultProject/Gaussian_C11470169729/file.txt");
                return identifier;
            case "2":
                identifier.setHost("pgadev.scigap.org");
                identifier.setUser("pga");
                identifier.setPort(22);
                identifier.setKeyFile("/Users/dwannipu/.ssh/id_rsa");
                identifier.setKeyPassphrase(null);
                identifier.setRemotePath("/var/www/portals/gateway-user-data/dev-seagrid/eromads6/DefaultProject/Gaussian_C11470169729/new-file.txt");
                return identifier;
        }
        return null;
    }

    public static Session createSession(String user, String host, int port, String keyFilePath, String keyPassword) {
        try {
            JSch jsch = new JSch();

            if (keyFilePath != null) {
                if (keyPassword != null) {
                    jsch.addIdentity(keyFilePath, keyPassword);
                } else {
                    jsch.addIdentity(keyFilePath);
                }
            }

            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, host, port);
            session.setConfig(config);
            session.connect();

            return session;
        } catch (JSchException e) {
            System.out.println(e);
            return null;
        }
    }
}
