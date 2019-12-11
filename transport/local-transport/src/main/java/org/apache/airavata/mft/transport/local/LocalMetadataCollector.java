package org.apache.airavata.mft.transport.local;

import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.MetadataCollector;

import java.io.File;

public class LocalMetadataCollector implements MetadataCollector {
    @Override
    public ResourceMetadata getGetResourceMetadata(String resourceId, String credentialToken) throws Exception {

        LocalResourceIdentifier resource = LocalTransportUtil.getLocalResourceIdentifier(resourceId);
        File file = new File(resource.getPath());

        ResourceMetadata metadata = new ResourceMetadata();
        metadata.setResourceSize(file.length());
        metadata.setUpdateTime(file.lastModified());
        return metadata;
    }
}
