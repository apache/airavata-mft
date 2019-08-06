package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.core.streaming.TransportStream;

public interface StreamedSender {
    public void send(String resourceIdentifier, TransportStream stream) throws Exception;
}
