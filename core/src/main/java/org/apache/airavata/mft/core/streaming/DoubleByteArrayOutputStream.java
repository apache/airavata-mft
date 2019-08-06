package org.apache.airavata.mft.core.streaming;

import java.io.*;

public class DoubleByteArrayOutputStream extends OutputStream {

    private ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
    private ByteArrayOutputStream stream2 = new ByteArrayOutputStream();

    private ByteArrayOutputStream currentStream = stream1;
    private int activeStream = 1;

    private long maxBytesPerStream = 1 * 1000 * 1000;
    private long processedBytes = 0;
    private boolean clearedNonActiveStream = false;

    @Override
    public void write(int b) throws IOException {
        this.currentStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (processedBytes > maxBytesPerStream) {
            while (!clearedNonActiveStream) {
                try {
                    Thread.sleep(100);
                    System.out.println("Waiting until non active buffer gets emptied");
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            if (activeStream == 1) {
                activeStream = 2;
                currentStream = stream2;
            } else {
                activeStream = 1;
                currentStream = stream1;
            }
            currentStream.reset();
            processedBytes = 0;
            clearedNonActiveStream = false;
        }
        this.currentStream.write(b, off, len);
        processedBytes += len;
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.currentStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        this.currentStream.flush();
    }

    @Override
    public void close() throws IOException {
        this.stream1.close();
        this.stream2.close();
    }

    public InputStream asInputStream() {
        if (clearedNonActiveStream) {
            return null;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(activeStream == 1 ? stream2.toByteArray(): stream1.toByteArray());
        this.clearedNonActiveStream = true;
        return inputStream;
    }
}
