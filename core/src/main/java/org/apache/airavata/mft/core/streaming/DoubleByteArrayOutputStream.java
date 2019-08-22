/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.mft.core.streaming;

import java.io.*;

/**
 * This class wraps two {@link ByteArrayOutputStream} objects and implements the API of {@link OutputStream}. Main
 * objective of this implementation is to use one stream for writing data and other stream to read the already written data.
 * Behaviour of this Class is similar to what {@link PipedOutputStream} is doing but with more relaxed modes in thread
 * contention and high performance. Limitation is that an instance of this class only be used between two threads. One
 * thread can write to the output stream and other thread can digest data from asInputStream method.
 */
public class DoubleByteArrayOutputStream extends OutputStream {

    private ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
    private ByteArrayOutputStream stream2 = new ByteArrayOutputStream();

    private ByteArrayOutputStream currentStream = stream1;
    private int activeStream = 1;

    private long maxBytesPerStream = 1 * 1000 * 1000;
    private long processedBytes = 0;
    private long totalBytes = 0;
    private boolean clearedNonActiveStream = false;

    private boolean isClosed = false;

    @Override
    public void write(int b) throws IOException {
        if (processedBytes > maxBytesPerStream) {
            swapBuffers();
        }
        this.currentStream.write(b);
        processedBytes += 1;
        totalBytes += 1;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (processedBytes > maxBytesPerStream) {
            swapBuffers();
        }
        this.currentStream.write(b, off, len);
        processedBytes += len;
        totalBytes += len;
    }

    /**
     * This will swap currently writing {@link BufferedOutputStream} into the non active one. In order to perform
     * this swap, non active stream should be empty.
     */
    private void swapBuffers() {
        while (!clearedNonActiveStream) {
            try {
                Thread.sleep(100);
                System.out.println("Receiver waiting until non active buffer gets emptied " + processedBytes);
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
        if (!isClosed) {
            System.out.println("Swapping buffers...");
            swapBuffers();
            //this.stream1.close();
            //this.stream2.close();
        }
        System.out.println("Output stream received total bytes " + totalBytes);
        isClosed = true;
    }

    /**
     * This will read currently non updating output stream and convert its data as an {@link InputStream}. Multiple invoking
     * this will return null if there is no new data.
     * @return {@link InputStream} of currently un-read data. null otherwise
     */
    public InputStream asInputStream() {
        if (clearedNonActiveStream) {
            return null;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(activeStream == 1 ? stream2.toByteArray(): stream1.toByteArray());
        this.clearedNonActiveStream = true;
        return inputStream;
    }

    public boolean isClosed() {
        return isClosed;
    }
}
