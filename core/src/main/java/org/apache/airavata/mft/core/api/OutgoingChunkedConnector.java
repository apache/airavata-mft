package org.apache.airavata.mft.core.api;

import java.io.InputStream;

public interface OutgoingChunkedConnector extends BasicConnector {
    public void uploadChunk(int chunkId, long startByte, long endByte, String uploadFile) throws Exception;
    public void uploadChunk(int chunkId, long startByte, long endByte, InputStream inputStream) throws Exception;
}
