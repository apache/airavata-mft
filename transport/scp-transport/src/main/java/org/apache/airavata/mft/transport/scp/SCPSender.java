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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.DoubleStreamingBuffer;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecret;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecretGetRequest;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.client.ResourceServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.common.GenericResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResourceGetRequest;
import org.apache.airavata.mft.resource.stubs.scp.storage.SCPStorage;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class SCPSender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(SCPSender.class);

    boolean initialized = false;

    private Session session;
    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;

    public void init(String resourceServiceHost, int resourceServicePort,
                     String secretServiceHost, int secretServicePort) throws Exception {

        this.resourceServiceHost = resourceServiceHost;
        this.resourceServicePort = resourceServicePort;
        this.secretServiceHost = secretServiceHost;
        this.secretServicePort = secretServicePort;

        if (initialized) {
            destroy();
        }
        this.initialized = true;
    }


    public void destroy() {

        try {
            this.session.disconnect();
        } catch (Exception e) {
            logger.error("Errored while disconnecting session", e);
        }
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("SCP Sender is not initialized");
        }
    }

    public void startStream(AuthToken authToken, String resourceId, String credentialToken, ConnectorContext context) throws Exception {
        startStream(authToken, resourceId, null, credentialToken, context);
    }

    @Override
    public void startStream(AuthToken authToken, String resourceId, String childResourcePath, String credentialToken, ConnectorContext context) throws Exception {
        checkInitialized();

        GenericResource resource;
        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort)) {
            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(authToken).setResourceId(resourceId).build());
        }

        if (resource.getStorageCase() != GenericResource.StorageCase.SCPSTORAGE) {
            logger.error("Invalid storage type {} specified for resource {}", resource.getStorageCase(), resourceId);
            throw new Exception("Invalid storage type specified for resource " + resourceId);
        }

        SCPSecret scpSecret;

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort)) {
            scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                    .setAuthzToken(authToken)
                    .setSecretId(credentialToken).build());
        }

        SCPStorage scpStorage = resource.getScpStorage();
        logger.info("Creating a ssh session for {}@{}:{}", scpSecret.getUser(), scpStorage.getHost(), scpStorage.getPort());

        this.session = SCPTransportUtil.createSession(
                scpSecret.getUser(),
                scpStorage.getHost(),
                scpStorage.getPort(),
                scpSecret.getPrivateKey().getBytes(),
                scpSecret.getPublicKey().getBytes(),
                scpSecret.getPassphrase().equals("")? null : scpSecret.getPassphrase().getBytes());

        if (session == null) {
            System.out.println("Session can not be null. Make sure that SCP Sender is properly initialized");
            throw new Exception("Session can not be null. Make sure that SCP Sender is properly initialized");
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
                throw new Exception("Resource was not set in resource with id " + resourceId);
        }

        try {
            copyLocalToRemote(this.session,
                    resourcePath,
                    context.getStreamBuffer(),
                    context.getMetadata().getResourceSize());
            logger.info("SCP send to transfer {} completed", context.getTransferId());

        } catch (Exception e) {
            logger.error("Errored while streaming to remote scp server. Transfer {}", context.getTransferId() , e);
            throw e;
        }
    }

    private void copyLocalToRemote(Session session, String to, DoubleStreamingBuffer streamBuffer, long fileSize) throws JSchException, IOException {
        try {
            logger.info("Starting scp send for remote server");
            InputStream inputStream = streamBuffer.getInputStream();

            boolean ptimestamp = true;

            // exec 'scp -t rfile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + to;
            com.jcraft.jsch.Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                throw new IOException("Error code found in ack " + (checkAck(in)));
            }

            if (ptimestamp) {
                command = "T" + (System.currentTimeMillis() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (System.currentTimeMillis() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    throw new IOException("Error code found in ack " + (checkAck(in)));
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            command = "C0644 " + fileSize + " ";
            if (to.lastIndexOf('/') > 0) {
                command += to.substring(to.lastIndexOf('/') + 1);
            } else {
                command += to;
            }

            command += "\n";
            out.write(command.getBytes());
            out.flush();

            if (checkAck(in) != 0) {
                throw new IOException("Error code found in ack " + (checkAck(in)));
            }

            // send a content of lfile
            byte[] buf = new byte[1024];
            long totalWritten = 0;
            while (true) {
                int len = inputStream.read(buf, 0, buf.length);
                if (len == -1) {
                    break;
                } else {
                    out.write(buf, 0, len); //out.flush();
                    totalWritten += len;
                    //System.out.println("Write " + totalWritten);
                    if (totalWritten == fileSize) {
                        break;
                    }
                }
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            if (checkAck(in) != 0) {
                throw new IOException("Error code found in ack " + (checkAck(in)));
            }
            out.close();
            channel.disconnect();

        } finally {
            try {
                session.disconnect();
            } catch (Exception e) {
                logger.warn("Session disconnection failed", e);
            }
        }
    }

    public int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //         -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
