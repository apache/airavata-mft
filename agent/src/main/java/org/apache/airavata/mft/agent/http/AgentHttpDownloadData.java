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

import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingConnector;

public class AgentHttpDownloadData {
    private IncomingConnector incomingConnector;
    private ConnectorConfig connectorConfig;
    private String childResourcePath;
    private long createdTime = System.currentTimeMillis();

    public IncomingConnector getIncomingConnector() {
        return incomingConnector;
    }

    public void setIncomingConnector(IncomingConnector incomingConnector) {
        this.incomingConnector = incomingConnector;
    }

    public ConnectorConfig getConnectorConfig() {
        return connectorConfig;
    }

    public void setConnectorConfig(ConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    public String getChildResourcePath() {
        return childResourcePath;
    }

    public void setChildResourcePath(String childResourcePath) {
        this.childResourcePath = childResourcePath;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }


    public static final class AgentHttpDownloadDataBuilder {
        private IncomingConnector incomingConnector;
        private ConnectorConfig connectorConfig;
        private String childResourcePath;
        private long createdTime = System.currentTimeMillis();

        private AgentHttpDownloadDataBuilder() {
        }

        public static AgentHttpDownloadDataBuilder newBuilder() {
            return new AgentHttpDownloadDataBuilder();
        }

        public AgentHttpDownloadDataBuilder withIncomingConnector(IncomingConnector incomingConnector) {
            this.incomingConnector = incomingConnector;
            return this;
        }

        public AgentHttpDownloadDataBuilder withConnectorConfig(ConnectorConfig connectorConfig) {
            this.connectorConfig = connectorConfig;
            return this;
        }

        public AgentHttpDownloadDataBuilder withChildResourcePath(String childResourcePath) {
            this.childResourcePath = childResourcePath;
            return this;
        }

        public AgentHttpDownloadDataBuilder withCreatedTime(long createdTime) {
            this.createdTime = createdTime;
            return this;
        }


        public AgentHttpDownloadData build() {
            AgentHttpDownloadData agentHttpDownloadData = new AgentHttpDownloadData();
            agentHttpDownloadData.setIncomingConnector(incomingConnector);
            agentHttpDownloadData.setConnectorConfig(connectorConfig);
            agentHttpDownloadData.setChildResourcePath(childResourcePath);
            agentHttpDownloadData.setCreatedTime(createdTime);
            return agentHttpDownloadData;
        }
    }
}
