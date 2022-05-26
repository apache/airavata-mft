package org.apache.airavata.mft.core.api;

public interface BasicConnector {
    public void init(ConnectorConfig connectorConfig) throws Exception;
    public void complete() throws Exception;
}
