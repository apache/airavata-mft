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

package org.apache.airavata.mft.agent.http;

public class ConnectorParams {

    private String storageId, credentialToken, resourceServiceHost, secretServiceHost;
    private int resourceServicePort, secretServicePort;

    public String getStorageId() {
        return storageId;
    }

    public ConnectorParams setStorageId(String storageId) {
        this.storageId = storageId;
        return this;
    }

    public String getCredentialToken() {
        return credentialToken;
    }

    public ConnectorParams setCredentialToken(String credentialToken) {
        this.credentialToken = credentialToken;
        return this;
    }

    public String getResourceServiceHost() {
        return resourceServiceHost;
    }

    public ConnectorParams setResourceServiceHost(String resourceServiceHost) {
        this.resourceServiceHost = resourceServiceHost;
        return this;
    }

    public String getSecretServiceHost() {
        return secretServiceHost;
    }

    public ConnectorParams setSecretServiceHost(String secretServiceHost) {
        this.secretServiceHost = secretServiceHost;
        return this;
    }

    public int getResourceServicePort() {
        return resourceServicePort;
    }

    public ConnectorParams setResourceServicePort(int resourceServicePort) {
        this.resourceServicePort = resourceServicePort;
        return this;
    }

    public int getSecretServicePort() {
        return secretServicePort;
    }

    public ConnectorParams setSecretServicePort(int secretServicePort) {
        this.secretServicePort = secretServicePort;
        return this;
    }
}
