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
import com.jcraft.jsch.Session;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.DoubleStreamingBuffer;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.*;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.SCPSecret;
import org.apache.airavata.mft.secret.service.SCPSecretGetRequest;
import org.apache.airavata.mft.secret.service.SecretServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class SCPReceiver implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(SCPReceiver.class);

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

        this.session = SCPTransportUtil.createSession(
                scpResource.getScpStorage().getUser(),
                scpResource.getScpStorage().getHost(),
                scpResource.getScpStorage().getPort(),
                scpSecret.getPrivateKey().getBytes(),
                scpSecret.getPublicKey().getBytes(),
                scpSecret.getPassphrase().equals("")? null : scpSecret.getPassphrase().getBytes());
    }

    public void destroy() {

    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("SCP Receiver is not initialized");
        }
    }

    public void startStream(ConnectorContext context) throws Exception {
        checkInitialized();
        if (session == null) {
            System.out.println("Session can not be null. Make sure that SCP Receiver is properly initialized");
            throw new Exception("Session can not be null. Make sure that SCP Receiver is properly initialized");
        }

        transferRemoteToStream(session, this.scpResource.getResourcePath(), context.getStreamBuffer());
        logger.info("SCP Receive completed. Transfer {}", context.getTransferId());
    }

    private void transferRemoteToStream(Session session, String from, DoubleStreamingBuffer streamBuffer) throws Exception {

        try {
            OutputStream outputStream = streamBuffer.getOutputStream();

            logger.info("Starting scp receive");
            // exec 'scp -f rfile' remotely
            String command = "scp -f " + from;
            com.jcraft.jsch.Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }

                // read '0644 '
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ') break;
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                String file = null;
                for (int i = 0; ; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte) 0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }

                logger.info("file-size=" + filesize + ", file=" + file);
                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                // read a content of lfile
                int bufSize;
                while (true) {
                    if (buf.length < filesize) bufSize = buf.length;
                    else bufSize = (int) filesize;
                    bufSize = in.read(buf, 0, bufSize);
                    if (bufSize < 0) {
                        // error
                        break;
                    }
                    //System.out.println("Read " + bufSize);
                    outputStream.write(buf, 0, bufSize);
                    outputStream.flush();

                    filesize -= bufSize;
                    if (filesize == 0L) break;
                }

                if (checkAck(in) != 0) {
                    throw new IOException("Error code found in ack " + (checkAck(in)));
                }

                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
                outputStream.close();
            }

            channel.disconnect();
            session.disconnect();

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
