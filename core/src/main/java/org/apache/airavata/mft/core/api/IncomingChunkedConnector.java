package org.apache.airavata.mft.core.api;

public interface IncomingChunkedConnector extends BasicConnector {
    public void downloadChunk(int chunkId, long startByte, long endByte, String downloadFile) throws Exception;
}
