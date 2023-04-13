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

package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.agent.stub.ResourceMetadata;
import org.apache.airavata.mft.agent.stub.SecretWrapper;
import org.apache.airavata.mft.agent.stub.StorageWrapper;

import java.util.Map;

public class ConnectorConfig {
    private String transferId;
    private StorageWrapper storage;
    private SecretWrapper secret;
    private String resourcePath;
    private ResourceMetadata metadata;

    private Map transportConfig;

    private long chunkSize;

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public StorageWrapper getStorage() {
        return storage;
    }

    public void setStorage(StorageWrapper storage) {
        this.storage = storage;
    }

    public SecretWrapper getSecret() {
        return secret;
    }

    public void setSecret(SecretWrapper secret) {
        this.secret = secret;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public ResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
        this.metadata = metadata;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Map getTransportConfig() {
        return transportConfig;
    }

    public void setTransportConfig(Map transportConfig) {
        this.transportConfig = transportConfig;
    }

    public static final class LocalConfigs {
        public LocalConfigs() {
        }

        public final static String DMA_ENABLED = "local.dma";
        public final static String BUFF_LEN = "local.buffLen";
    }

    public Boolean getBooleanTransportProperty(String key, Boolean defaultValue){
        if(key == LocalConfigs.DMA_ENABLED){
            if (!this.transportConfig.get(key).toString().isEmpty()) return Boolean.getBoolean(this.transportConfig.get(key).toString());
        }
        return defaultValue;
    }

    public int getIntTransportProperty(String key, int defaultValue){
        if(key == LocalConfigs.BUFF_LEN){
            if (!this.transportConfig.get(key).toString().isEmpty()) return Integer.valueOf(this.transportConfig.get(key).toString());
        }
        return defaultValue;
    }

    public Long getLongTransportProperty(String key, Long defaultValue){
        return defaultValue;
    }

    public double getDoubleTransportProperty(String key, double defaultValue){
        return defaultValue;
    }

    public String getStringTransportProperty(String key, String defaultValue){
        return defaultValue;
    }

    public static final class ConnectorConfigBuilder {
        private String transferId;
        private StorageWrapper storage;
        private SecretWrapper secret;
        private String resourcePath;
        private ResourceMetadata metadata;
        private Map transportConfig;

        private long chunkSize;

        private ConnectorConfigBuilder() {
        }

        public static ConnectorConfigBuilder newBuilder() {
            return new ConnectorConfigBuilder();
        }

        public ConnectorConfigBuilder withTransferId(String transferId) {
            this.transferId = transferId;
            return this;
        }

        public ConnectorConfigBuilder withStorage(StorageWrapper storage) {
            this.storage = storage;
            return this;
        }

        public ConnectorConfigBuilder withSecret(SecretWrapper secret) {
            this.secret = secret;
            return this;
        }

        public ConnectorConfigBuilder withResourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        public ConnectorConfigBuilder withMetadata(ResourceMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public ConnectorConfigBuilder withTransportConfig(Map transportConfig) {
            this.transportConfig = transportConfig;
            return this;
        }

        public ConnectorConfigBuilder withChunkSize(long chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public ConnectorConfig build() {
            ConnectorConfig connectorConfig = new ConnectorConfig();
            connectorConfig.setTransferId(transferId);
            connectorConfig.setStorage(storage);
            connectorConfig.setSecret(secret);
            connectorConfig.setResourcePath(resourcePath);
            connectorConfig.setMetadata(metadata);
            connectorConfig.setChunkSize(chunkSize);
            connectorConfig.setTransportConfig(transportConfig);
            return connectorConfig;
        }
    }
}
