package org.apache.airavata.mft.core;

import net.ladenthin.streambuffer.StreamBuffer;

public class ConnectorContext {

    private StreamBuffer streamBuffer;
    private ResourceMetadata metadata;

    public StreamBuffer getStreamBuffer() {
        return streamBuffer;
    }

    public void setStreamBuffer(StreamBuffer streamBuffer) {
        this.streamBuffer = streamBuffer;
    }

    public ResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
        this.metadata = metadata;
    }
}
