package org.apache.airavata.mft.core.streaming;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransportStream {

    private OutputStream outputStream = new DoubleByteArrayOutputStream();
    private InputStream inputStream = new DoubleByteArrayInputStream((DoubleByteArrayOutputStream) outputStream);
    private long length = -1;
    private AtomicBoolean streamCompleted = new AtomicBoolean(false);

    public TransportStream() throws IOException {
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public AtomicBoolean getStreamCompleted() {
        return streamCompleted;
    }

    public void setStreamCompleted(AtomicBoolean streamCompleted) {
        this.streamCompleted = streamCompleted;
    }
}
