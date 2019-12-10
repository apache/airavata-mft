package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.core.ConnectorContext;

public interface Connector {
    public void init(String resourceId, String credentialToken) throws Exception;
    public void destroy();
    void startStream(ConnectorContext context) throws Exception;
}
