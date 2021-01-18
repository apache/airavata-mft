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

import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;

public class HttpTransferRequest {
    private Connector otherConnector;
    private MetadataCollector otherMetadataCollector;
    private ConnectorParams connectorParams;
    private String targetResourcePath;

    public Connector getOtherConnector() {
        return otherConnector;
    }

    public HttpTransferRequest setOtherConnector(Connector otherConnector) {
        this.otherConnector = otherConnector;
        return this;
    }

    public MetadataCollector getOtherMetadataCollector() {
        return otherMetadataCollector;
    }

    public HttpTransferRequest setOtherMetadataCollector(MetadataCollector otherMetadataCollector) {
        this.otherMetadataCollector = otherMetadataCollector;
        return this;
    }

    public String getTargetResourcePath() {
        return targetResourcePath;
    }

    public HttpTransferRequest setTargetResourcePath(String targetResourcePath) {
        this.targetResourcePath = targetResourcePath;
        return this;
    }

    public ConnectorParams getConnectorParams() {
        return connectorParams;
    }

    public HttpTransferRequest setConnectorParams(ConnectorParams connectorParams) {
        this.connectorParams = connectorParams;
        return this;
    }
}
