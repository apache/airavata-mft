package org.apache.airavata.mft.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class DoubleStreamingBuffer {
    int bufferSize = 2048;

    private OutputStream outputStream = new DoubleStreamingBuffer.DSBOutputStream();
    private InputStream inputStream = new DoubleStreamingBuffer.DSBInputStream();

    CyclicBarrier barrier = new CyclicBarrier(2);

    final byte[] buffer1 = new byte[bufferSize];
    final byte[] buffer2 = new byte[bufferSize];

    int buf1Remain = 0;
    int buf2Remain = 0;

    ReentrantLock buffer1Lock = new ReentrantLock();
    ReentrantLock buffer2Lock = new ReentrantLock();

    boolean readBuffer1 = true;
    boolean doneWrite = false;
    int readPoint = 0;

    boolean barrierPassed = false;

    public class DSBOutputStream extends OutputStream {

        @Override
        public void close() throws IOException {
            doneWrite = true;
            System.out.println("Closing");
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
                        barrier.await();
                    } catch (Exception e) {
                        throw new IOException();
                    }
                    //return read();
                    buf2Remain --;
                    return buffer2[readPoint++] & 0xff;
                }
            } else {
                if (buf2Remain > 0) {
                    buf2Remain --;
                    //System.out.println("Readval " + (buffer2[readPoint] & 0xff));

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
                        barrier.await();
                    } catch (Exception e) {
                        throw new IOException();
                    }
                    //return read();
                    buf1Remain --;
                    return buffer1[readPoint++] & 0xff;
                }
            }

        }
    }


    public static void main(String args[]) throws InterruptedException {
        DoubleStreamingBuffer dsb = new DoubleStreamingBuffer();
        CyclicBarrier barrier = new CyclicBarrier(2);

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 1");
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("Done Thread 1");
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread 2");
                try {
                    Thread.sleep(5000);
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("Done Thread 2");
            }
        });


        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

    }


    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
