package org.apache.airavata.mft.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Thread safe byte buffer bridging bytes from a output stream to a input stream.
 * This is an alternative for {@link CircularStreamingBuffer} to avoid synchronization overhead among the read thread
 * and the write thread. This has a separate input and output stream that should be utilized by two different threads.
 * Bytes are copied through two internal byte arrays. Always one array is dedicated to writes and another for reads. Once
 * the write buffer is full and read buffer is empty, read and write threads swap the buffers. This is the only placed where
 * synchronization is enforced.
 */
public class DoubleStreamingBuffer {

    /*
    Size of the internal arrays
     */
    private int bufferSize = 2048;

    /*
    Internal buffers
     */
    private final byte[] buffer1 = new byte[bufferSize];
    private final byte[] buffer2 = new byte[bufferSize];

    private OutputStream outputStream = new DoubleStreamingBuffer.DSBOutputStream();
    private InputStream inputStream = new DoubleStreamingBuffer.DSBInputStream();

    private CyclicBarrier barrier = new CyclicBarrier(2);

    /*
    Remaining bytes in each buffer available for read. Read thread subtracts the count once read and write threads
    increases the count for writes
     */
    private int buf1Remain = 0;
    private int buf2Remain = 0;

    private ReentrantLock buffer1Lock = new ReentrantLock();
    private ReentrantLock buffer2Lock = new ReentrantLock();

    boolean readBuffer1 = true;
    boolean doneWrite = false;
    int readPoint = 0;

    boolean barrierPassed = false;

    private long processedBytes = 0L;

    public class DSBOutputStream extends OutputStream {

        @Override
        public void close() throws IOException {
            doneWrite = true;
            if (readBuffer1) {
                buffer2Lock.unlock();
            } else {
                buffer1Lock.unlock();
            }
            try {
                barrier.await();
            } catch (Exception e) {
                throw new IOException();
            }
        }

        @Override
        public void write(int b) throws IOException {

            if (!barrierPassed) {
                try {
                    if (readBuffer1) {
                        buffer2Lock.lock();
                    } else {
                        buffer1Lock.lock();
                    }

                    // wait for reader to enter into read block for the first time
                    barrier.await();

                    barrierPassed = true;
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }

            //System.out.println("Write " + readBuffer1 + " " + buf1Remain + " " + buf2Remain);
            if (readBuffer1) {
                if (buf2Remain < bufferSize) {
                    buffer2[buf2Remain] = (byte)b;
                    buf2Remain ++;
                } else {
                    barrier.reset();
                    buffer2Lock.unlock();
                    buffer1Lock.lock();
                    try {
                        // Wait for reader to move into next buffer
                        barrier.await();
                    } catch (Exception e) {
                        throw new IOException();
                    }
                    write(b);
                }
            } else {
                if (buf1Remain < bufferSize) {
                    buffer1[buf1Remain] = (byte)b;
                    buf1Remain++;
                } else {
                    barrier.reset();
                    buffer1Lock.unlock();
                    buffer2Lock.lock();
                    try {
                        // Wait for reader to move into next buffer
                        barrier.await();
                    } catch (Exception e) {
                        throw new IOException();
                    }
                    write(b);
                }
            }
        }
    }

    public class DSBInputStream extends InputStream {

        @Override
        public int read() throws IOException {

            if (!barrierPassed) {
                try {
                    if (readBuffer1) {
                        buffer1Lock.lock();
                    } else {
                        buffer2Lock.lock();
                    }

                    // wait for writer to enter into read block for the first time
                    barrier.await();

                    barrierPassed = true;
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }

            //System.out.println("Read " + readBuffer1 + " " + buf1Remain + " " + buf2Remain);

            if (readBuffer1) {
                if (buf1Remain > 0) {
                    buf1Remain --;
                    //System.out.println("Readval " + (buffer1[readPoint] & 0xff));
                    processedBytes++;
                    return buffer1[readPoint++] & 0xff;
                } else {

                    if (doneWrite && buf2Remain <= 0) {
                        //System.out.println("Return -1");
                        return -1;
                    }
                    buffer2Lock.lock();
                    readBuffer1 = false;
                    buffer1Lock.unlock();

                    readPoint = 0;
                    try {
                        // Wait for writer to move into next buffer
                        barrier.await();
                    } catch (Exception e) {
                        throw new IOException();
                    }
                    //return read();
                    buf2Remain --;
                    processedBytes++;
                    return buffer2[readPoint++] & 0xff;
                }
            } else {
                if (buf2Remain > 0) {
                    buf2Remain --;
                    //System.out.println("Readval " + (buffer2[readPoint] & 0xff));
                    processedBytes++;
                    return buffer2[readPoint++] & 0xff;
                } else {

                    if (doneWrite && buf1Remain <= 0) {
                        //System.out.println("Return -1");
                        return -1;
                    }
                    buffer1Lock.lock();
                    readBuffer1 = true;
                    buffer2Lock.unlock();
                    readPoint = 0;
                    try {
                        // Wait for writer to move into next buffer
                        barrier.await();
                    } catch (Exception e) {
                        throw new IOException();
                    }
                    //return read();
                    buf1Remain --;
                    processedBytes++;
                    return buffer1[readPoint++] & 0xff;
                }
            }

        }
    }


    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public long getProcessedBytes() {
        return processedBytes;
    }
}
