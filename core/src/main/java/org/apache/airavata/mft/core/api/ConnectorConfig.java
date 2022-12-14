package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.agent.stub.ResourceMetadata;
import org.apache.airavata.mft.agent.stub.SecretWrapper;
import org.apache.airavata.mft.agent.stub.StorageWrapper;

public class ConnectorConfig {
    private String transferId;
    private StorageWrapper storage;
    private SecretWrapper secret;
    private String resourcePath;
    private ResourceMetadata metadata;

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


    public static final class ConnectorConfigBuilder {
        private String transferId;
        private StorageWrapper storage;
        private SecretWrapper secret;
        private String resourcePath;
        private ResourceMetadata metadata;

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

        public ConnectorConfig build() {
            ConnectorConfig connectorConfig = new ConnectorConfig();
            connectorConfig.setTransferId(transferId);
            connectorConfig.setStorage(storage);
            connectorConfig.setSecret(secret);
            connectorConfig.setResourcePath(resourcePath);
            connectorConfig.setMetadata(metadata);
            return connectorConfig;
        }
    }
}
