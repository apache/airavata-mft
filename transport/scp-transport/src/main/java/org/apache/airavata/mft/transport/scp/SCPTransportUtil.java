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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;

public class SCPTransportUtil {

    private static final Logger logger = LoggerFactory.getLogger(SCPTransportUtil.class);

    public static Session createSession(String user, String host, int port, byte[] pvtKey, byte[] pubKey, byte[] passphrase) throws Exception {
        try {
            JSch jsch = new JSch();

            jsch.addIdentity(UUID.randomUUID().toString(), pvtKey, pubKey, passphrase);

            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, host, port);
            session.setConfig(config);
            session.connect();

            return session;
        } catch (JSchException e) {
            throw new Exception("Failed to create a ssh session for " + user + "@" + host + ":" + port, e);
        }
    }
}
