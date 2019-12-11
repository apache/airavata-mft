package org.apache.airavata.mft.core;

public class ConnectorContext {

    private CircularStreamingBuffer streamBuffer;
    private ResourceMetadata metadata;

    public CircularStreamingBuffer getStreamBuffer() {
        return streamBuffer;
    }

    public void setStreamBuffer(CircularStreamingBuffer streamBuffer) {
        this.streamBuffer = streamBuffer;
    }

    public ResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
        this.metadata = metadata;
    }
}
