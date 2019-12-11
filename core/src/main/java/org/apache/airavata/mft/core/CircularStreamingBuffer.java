package org.apache.airavata.mft.core;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

public class CircularStreamingBuffer {

    private int bufferSize = 1024;
    private ArrayBlockingQueue<Byte> buffer = new ArrayBlockingQueue<>(bufferSize);

    private boolean osClosed = false;
    private Semaphore readSem = new Semaphore(0);

    private OutputStream outputStream = new CSBOutputStream();
    private InputStream inputStream = new CSBInputStream();

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    private void updateRead() {
        if (readSem.availablePermits() == 0) {
            readSem.release();
        }
    }

    public class CSBOutputStream extends OutputStream {
        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (int i = off; i < len; i ++) {
                try {
                    buffer.put(b[i]);
                    updateRead();
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            osClosed = true;
            updateRead();
        }

        @Override
        public void write(int b) throws IOException {
            try {
                buffer.put((byte)b);
                //System.out.println("Writing");
                updateRead();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }

    public class CSBInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            Byte res = buffer.poll();
            if (res == null) {
                //System.out.println("Received null in is.read()");
                if (osClosed) return -1;
                try {
                    readSem.acquire();
                    return read();
                } catch (InterruptedException e) {
                    throw new IOException(e);
                }
            } else {
                return res & 0xff;
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            for (int i = off; i < len; i++) {
                int res = read();
                if (res == -1) {
                    //System.out.println("Received -1 in is.read(byte[], off, len)");
                    if (i == off) {
                        //System.out.println("Return -1");
                        return -1;
                    } else {
                        //System.out.println("Return " + (i - off));
                        return i - off;
                    }
                } else {
                    b[i] = (byte)res;
                }
            }
            return len - off;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
    }
}