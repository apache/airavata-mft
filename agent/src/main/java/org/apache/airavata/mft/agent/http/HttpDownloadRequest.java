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

public class HttpDownloadRequest {

    private ConnectorParams connectorParams;
    private Connector srcConnector;
    private MetadataCollector srcMetadataCollector;
    private String srcResourceId;
    private String srcToken;

    public ConnectorParams getConnectorParams() {
        return connectorParams;
    }

    public HttpDownloadRequest setConnectorParams(ConnectorParams connectorParams) {
        this.connectorParams = connectorParams;
        return this;
    }

    public Connector getSrcConnector() {
        return srcConnector;
    }

    public HttpDownloadRequest setSrcConnector(Connector srcConnector) {
        this.srcConnector = srcConnector;
        return this;
    }

    public MetadataCollector getSrcMetadataCollector() {
        return srcMetadataCollector;
    }

    public HttpDownloadRequest setSrcMetadataCollector(MetadataCollector srcMetadataCollector) {
        this.srcMetadataCollector = srcMetadataCollector;
        return this;
    }

    public String getSrcResourceId() {
        return srcResourceId;
    }

    public HttpDownloadRequest setSrcResourceId(String srcResourceId) {
        this.srcResourceId = srcResourceId;
        return this;
    }

    public String getSrcToken() {
        return srcToken;
    }

    public HttpDownloadRequest setSrcToken(String srcToken) {
        this.srcToken = srcToken;
        return this;
    }
}
