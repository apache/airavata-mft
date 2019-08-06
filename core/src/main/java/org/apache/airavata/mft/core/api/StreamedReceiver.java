package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.core.streaming.TransportStream;

public interface StreamedReceiver {
    public void receive(String resourceIdentifier, TransportStream stream) throws Exception;
}
