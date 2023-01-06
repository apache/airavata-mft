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

package org.apache.airavata.mft.controller.spawner;

import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder;
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SSHProvider {

    private static final Logger logger = LoggerFactory.getLogger(SSHProvider.class);

    private SSHClient client;
    private String hostName;

    public void initConnection(String hostName, int port, String keyPath, String user) throws IOException {
        this.hostName = hostName;
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        client = new SSHClient(defaultConfig);
        client.addHostKeyVerifier(new HostKeyVerifier() {
            @Override
            public boolean verify(String s, int i, PublicKey publicKey) {
                return true;
            }

            @Override
            public List<String> findExistingAlgorithms(String s, int i) {
                return null;
            }
        });

        KeyProvider keyProvider = client.loadKeys(keyPath);

        final List<AuthMethod> am = new LinkedList<>();
        am.add(new AuthPublickey(keyProvider));

        am.add(new AuthKeyboardInteractive(new ChallengeResponseProvider() {
            @Override
            public List<String> getSubmethods() {
                return new ArrayList<>();
            }

            @Override
            public void init(Resource resource, String name, String instruction) {
            }

            @Override
            public char[] getResponse(String prompt, boolean echo) {
                return new char[0];
            }

            @Override
            public boolean shouldRetry() {
                return false;
            }
        }));

        client.connect(hostName, port);
        client.auth(user, am);
    }

    public void closeConnection() {
        try {
            if (client != null) {
                client.close();
                logger.info("Closed the SSH connection to host {}", hostName);
            }
        } catch (Throwable e) {
            logger.warn("Failed to close the SSH connection for host {}. Continuing ...", hostName, e);
        }
    }

    public int runCommand(String command) throws IOException {
        Session session = this.client.startSession();
        logger.info("Running command {}", command);
        Session.Command execResult = session.exec(command);
        String stdOut = new String(execResult.getInputStream().readAllBytes());
        String stdErr = new String(execResult.getErrorStream().readAllBytes());
        logger.info("Std out: {}", stdOut);
        logger.info("Std err: {}", stdErr);
        logger.info("Exit code: {}", execResult.getExitStatus());
        session.close();
        return execResult.getExitStatus();
    }

    public CountDownLatch createSshPortForward(int localPort, CountDownLatch portForwardHoldLock) throws IOException, InterruptedException {

        CountDownLatch portForwardCompleteLock = new CountDownLatch(1);
        new Thread(() -> {

            String consulHost = "localhost";
            RemotePortForwarder remotePortForwarder;
            RemotePortForwarder.Forward portBind;

            try {
                remotePortForwarder = client.getRemotePortForwarder();
                portBind = remotePortForwarder.bind(
                        new RemotePortForwarder.Forward(localPort),
                        new SocketForwardingConnectListener(new InetSocketAddress(consulHost, localPort)));

                portForwardCompleteLock.countDown();
                logger.info("Created port forward to port " + localPort);
                portForwardHoldLock.await();

                logger.info("Releasing the remote port forward");
                remotePortForwarder.cancel(portBind);

            } catch (Exception e) {
                logger.error("Failed to create the remote port forward for port {}", localPort, e);
            }
        }).start();
        return portForwardCompleteLock;
    }
}
