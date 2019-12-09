package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.core.ResourceMetadata;

public interface MetadataCollector {
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception;
}
