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
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.DoubleStreamingBuffer;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.SCPResource;
import org.apache.airavata.mft.resource.service.SCPResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.SCPSecret;
import org.apache.airavata.mft.secret.service.SCPSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class SCPSender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(SCPSender.class);

    boolean initialized = false;

    private Session session;
    private SCPResource scpResource;

    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort,
                     String secretServiceHost, int secretServicePort) throws Exception {

        this.initialized = true;

        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        this.scpResource = resourceClient.getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        SCPSecret scpSecret = secretClient.getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(credentialToken).build());

        logger.info("Creating a ssh session for {}@{}:{}",
                scpResource.getScpStorage().getUser(), scpResource.getScpStorage().getHost(),
                scpResource.getScpStorage().getPort());

        this.session = SCPTransportUtil.createSession(
                scpResource.getScpStorage().getUser(),
                scpResource.getScpStorage().getHost(),
                scpResource.getScpStorage().getPort(),
                scpSecret.getPrivateKey().getBytes(),
                scpSecret.getPublicKey().getBytes(),
                scpSecret.getPassphrase().equals("")? null : scpSecret.getPassphrase().getBytes());
    }

    public void destroy() {

        //try {
        //    this.session.disconnect();
        //} catch (Exception e) {
        //    logger.error("Errored while disconnecting session", e);
        //}
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("SCP Sender is not initialized");
        }
    }

    public void startStream(ConnectorContext context) throws Exception {

        checkInitialized();
        if (session == null) {
            System.out.println("Session can not be null. Make sure that SCP Sender is properly initialized");
            throw new Exception("Session can not be null. Make sure that SCP Sender is properly initialized");
        }
        try {
            copyLocalToRemote(this.session, this.scpResource.getResourcePath(), context.getStreamBuffer(), context.getMetadata().getResourceSize());
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
