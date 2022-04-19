package org.apache.airavata.mft.core.api;

import java.io.InputStream;

public interface IncomingChunkedConnector extends BasicConnector {
    public void downloadChunk(int chunkId, long startByte, long endByte, String downloadFile) throws Exception;
    public InputStream downloadChunk(int chunkId, long startByte, long endByte) throws Exception;
}
