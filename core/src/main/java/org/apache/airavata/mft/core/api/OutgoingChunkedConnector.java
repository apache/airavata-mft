package org.apache.airavata.mft.core.api;

public interface OutgoingChunkedConnector extends BasicConnector {
    public void uploadChunk(int chunkId, long startByte, long endByte, String uploadFile) throws Exception;
}
