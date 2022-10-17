package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.core.FileResourceMetadata;

public class ConnectorConfig {
    private String resourceServiceHost;
    private int resourceServicePort;
    private String secretServiceHost;
    private int secretServicePort;
    private String storageId;
    private String credentialToken;
    private AuthToken authToken;
    private String transferId;
    private FileResourceMetadata metadata;
    private String resourcePath;
    private String storageType;

    public String getResourceServiceHost() {
        return resourceServiceHost;
    }

    public void setResourceServiceHost(String resourceServiceHost) {
        this.resourceServiceHost = resourceServiceHost;
    }

    public int getResourceServicePort() {
        return resourceServicePort;
    }

    public void setResourceServicePort(int resourceServicePort) {
        this.resourceServicePort = resourceServicePort;
    }

    public String getSecretServiceHost() {
        return secretServiceHost;
    }

    public void setSecretServiceHost(String secretServiceHost) {
        this.secretServiceHost = secretServiceHost;
    }

    public int getSecretServicePort() {
        return secretServicePort;
    }

    public void setSecretServicePort(int secretServicePort) {
        this.secretServicePort = secretServicePort;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getCredentialToken() {
        return credentialToken;
    }

    public void setCredentialToken(String credentialToken) {
        this.credentialToken = credentialToken;
    }

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public FileResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FileResourceMetadata metadata) {
        this.metadata = metadata;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public static final class ConnectorConfigBuilder {
        private String resourceServiceHost;
        private int resourceServicePort;
        private String secretServiceHost;
        private int secretServicePort;
        private String stroageId;
        private String credentialToken;
        private AuthToken authToken;
        private String transferId;
        private String resourcePath;
        private FileResourceMetadata metadata;
        private String storageType;

        private ConnectorConfigBuilder() {
        }

        public static ConnectorConfigBuilder newBuilder() {
            return new ConnectorConfigBuilder();
        }

        public ConnectorConfigBuilder withResourceServiceHost(String resourceServiceHost) {
            this.resourceServiceHost = resourceServiceHost;
            return this;
        }

        public ConnectorConfigBuilder withResourceServicePort(int resourceServicePort) {
            this.resourceServicePort = resourceServicePort;
            return this;
        }

        public ConnectorConfigBuilder withSecretServiceHost(String secretServiceHost) {
            this.secretServiceHost = secretServiceHost;
            return this;
        }

        public ConnectorConfigBuilder withSecretServicePort(int secretServicePort) {
            this.secretServicePort = secretServicePort;
            return this;
        }

        public ConnectorConfigBuilder withStorageId(String storageId) {
            this.stroageId = storageId;
            return this;
        }

        public ConnectorConfigBuilder withResourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        public ConnectorConfigBuilder withStorageType(String storageType) {
            this.storageType = storageType;
            return this;
        }

        public ConnectorConfigBuilder withCredentialToken(String credentialToken) {
            this.credentialToken = credentialToken;
            return this;
        }

        public ConnectorConfigBuilder withAuthToken(AuthToken authToken) {
            this.authToken = authToken;
            return this;
        }

        public ConnectorConfigBuilder withTransferId(String transferId) {
            this.transferId = transferId;
            return this;
        }

        public ConnectorConfigBuilder withMetadata(FileResourceMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public ConnectorConfig build() {
            ConnectorConfig connectorConfig = new ConnectorConfig();
            connectorConfig.setResourceServiceHost(resourceServiceHost);
            connectorConfig.setResourceServicePort(resourceServicePort);
            connectorConfig.setSecretServiceHost(secretServiceHost);
            connectorConfig.setSecretServicePort(secretServicePort);
            connectorConfig.setStorageId(stroageId);
            connectorConfig.setStorageType(storageType);
            connectorConfig.setCredentialToken(credentialToken);
            connectorConfig.setAuthToken(authToken);
            connectorConfig.setTransferId(transferId);
            connectorConfig.setMetadata(metadata);
            connectorConfig.setResourcePath(resourcePath);
            return connectorConfig;
        }
    }
}
