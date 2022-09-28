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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class SCPIncomingConnector implements IncomingStreamingConnector {

    private static final Logger logger = LoggerFactory.getLogger(SCPIncomingConnector.class);

    private Session session;
    private GenericResource resource;
    private Channel channel;
    private OutputStream out;
    private InputStream in;
    private final byte[] buf = new byte[1024];

    @Override
    public void init(ConnectorConfig cc) throws Exception {

        try (ResourceServiceClient resourceClient = ResourceServiceClientBuilder
                .buildClient(cc.getResourceServiceHost(), cc.getResourceServicePort())) {

            resource = resourceClient.get().getGenericResource(GenericResourceGetRequest.newBuilder()
                    .setAuthzToken(cc.getAuthToken())
                    .setResourceId(cc.getResourceId()).build());
        }

        if (resource.getStorageCase() != GenericResource.StorageCase.SCPSTORAGE) {
            logger.error("Invalid storage type {} specified for resource {}", resource.getStorageCase(), cc.getResourceId());
            throw new Exception("Invalid storage type specified for resource " + cc.getResourceId());
        }

        SCPSecret scpSecret;

        try (SecretServiceClient secretClient = SecretServiceClientBuilder.buildClient(
                cc.getSecretServiceHost(), cc.getSecretServicePort())) {

            scpSecret = secretClient.scp().getSCPSecret(SCPSecretGetRequest.newBuilder()
                    .setAuthzToken(cc.getAuthToken())
                    .setSecretId(cc.getCredentialToken()).build());
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
            logger.error("Session can not be null. Make sure that SCP Receiver is properly initialized");
            throw new Exception("Session can not be null. Make sure that SCP Receiver is properly initialized");
        }
    }

    private String escapeSpecialChars(String path) {
        return  path.replace(" ", "\\ ");
    }

    @Override
    public InputStream fetchInputStream() throws Exception {
        String resourcePath = null;
        switch (resource.getResourceCase()){
            case FILE:
                resourcePath = resource.getFile().getResourcePath();
                break;
            case DIRECTORY:
                throw new Exception("A directory path can not be streamed");
            case RESOURCE_NOT_SET:
                throw new Exception("Resource was not set in resource with id " + resource.getResourceId());
        }

        return fetchInputStreamJCraft(escapeSpecialChars(resourcePath));
    }

    @Override
    public InputStream fetchInputStream(String childPath) throws Exception {

        String resourcePath = null;
        switch (resource.getResourceCase()){
            case FILE:
                throw new Exception("A child path can not be associated with a file parent");
            case DIRECTORY:
                resourcePath = resource.getDirectory().getResourcePath();
                if (!childPath.startsWith(resourcePath)) {
                    throw new Exception("Child path " + childPath + " is not in the parent path " + resourcePath);
                }
                resourcePath = childPath;
                break;
            case RESOURCE_NOT_SET:
                throw new Exception("Resource was not set in resource with id " + resource.getResourceId());
        }

        return fetchInputStreamJCraft(escapeSpecialChars(resourcePath));
    }

    private InputStream fetchInputStreamJCraft(String resourcePath) throws Exception{
        String command = "scp -f " + resourcePath;
        channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        out = channel.getOutputStream();
        in = channel.getInputStream();

        channel.connect();

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
            return new LimitInputStream(in, filesize);
        }
        return null;
    }

    @Override
    public void complete() throws Exception {
        if (checkAck(in) != 0) {
            throw new IOException("Error code found in ack " + (checkAck(in)));
        }

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        channel.disconnect();
        session.disconnect();
    }

    @Override
    public void failed() throws Exception {

    }

    private int checkAck(InputStream in) throws IOException {
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
                logger.error("Check Ack Failure b = 1 " + sb.toString());
            }
            if (b == 2) { // fatal error
                logger.error("Check Ack Failure b = 2 " + sb.toString());
            }
        }
        return b;
    }
}
