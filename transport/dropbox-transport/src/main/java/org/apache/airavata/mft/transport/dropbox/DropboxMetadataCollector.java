package org.apache.airavata.mft.transport.dropbox;

import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;

public class DropboxMetadataCollector implements MetadataCollector{
    @Override
    public void init(String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) {

    }

    @Override
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {
        return null;
    }

    @Override
    public Boolean isAvailable(String resourceId, String credentialToken) throws Exception {
        return null;
    }
}
