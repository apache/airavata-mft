// @formatter:off
/**
 * Copyright 2014 Bernard Ladenthin bernard.ladenthin@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
// @formatter:on
package org.apache.airavata.mft.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

// Copied from https://github.com/bernardladenthin/streambuffer
/**
 * A stream buffer is a class to buffer data that has been written to an
 * {@link OutputStream} and provide the data in an {@link InputStream}. There is
 * no need to close the stream before read. The read works during a write
 * process. It is possible to call concurrent a method in the
 * {@link OutputStream} and {@link InputStream}. Read/Write at the same time.
 *
 * @author Bernard Ladenthin bernard.ladenthin@gmail.com
 */
public class StreamBuffer implements Closeable {

    /**
     * An object to get an unique access to the {@link #buffer}. It is needed to
     * get an exclusive access for read and write operations.
     */
    private final Object bufferLock = new Object();

    /**
     * The buffer which contains the raw data.
     */
    private final Deque<byte[]> buffer = new LinkedList<>();

    /**
     * A {@link Semaphore} to signal that data are added to the {@link #buffer}.
     * This signal is also used to announce that the stream was closed.
     * This {@link Semaphore} is used only for internal communication.
     */
    private final Semaphore signalModification = new Semaphore(0);

    /**
     * A {@link Semaphore} to signal that data are added to the {@link #buffer}.
     * This signal is also used to announce that the stream was closed.
     * This {@link Semaphore} is used only for external communication.
     */
    private final Semaphore signalModificationExternal = new Semaphore(0);

    /**
     * A variable for the current position of the current element in the
     * {@link #buffer}.
     */
    private volatile int positionAtCurrentBufferEntry = 0;

    /**
     * The sum of available bytes.
     */
    private volatile long availableBytes = 0;

    /**
     * A flag to indicate the stream was closed.
     */
    private volatile boolean streamClosed = false;

    /**
     * A flag to enable a safe write. If safe write is enabled, modifiable byte
     * arrays are cloned before they are written (put) into the FIFO. Benefit:
     * It cost more performance to clone a byte array, but the content is
     * immutable. This may be revoked by {@link #ignoreSafeWrite}, for example a
     * byte array was created internally.
     */
    private volatile boolean safeWrite = false;

    /**
     * A flag to disable the {@link StreamBuffer#safeWrite}. I. e. to write a byte array from
     * the trim method.
     */
    private boolean ignoreSafeWrite = false;

    /**
     * The maximum buffer elements. A number of maximum elements to invoke the
     * trim method. If the buffer contains more elements as the maximum, the
     * buffer will be completely reduced to one element. This option can help to
     * improve the performance for a read of huge amount data. It can also help
     * to shrink the size of the memory used. Use a value lower or equals
     * <code>0</code> to disable the trim option. Default is a maximum of
     * <code>100</code> elements.
     */
    private volatile int maxBufferElements = 100;

    private final SBInputStream is = new SBInputStream();
    private final SBOutputStream os = new SBOutputStream();

    /**
     * Construct a new {@link StreamBuffer}.
     */
    public StreamBuffer() {
    }

    /**
     * Returns the flag which indicates whether write operations are executed
     * safe or unsafe.
     *
     * @return <code>true</code> if the safe write option is enabled, otherwise
     * <code>false</code>.
     */
    public boolean isSafeWrite() {
        return safeWrite;
    }

    /**
     * Set a secure or unsercure write operation.
     *
     * @param safeWrite A flag to enable a safe write. If safe write is enabled,
     * modifiable byte arrays are cloned before they are written. Benefit: It
     * cost more performance to clone a byte array, but it is hardened to
     * external changes. Use <code>true</code> to force a clone. Otherwise use
     * <code>false</code> (default).
     */
    public void setSafeWrite(boolean safeWrite) {
        this.safeWrite = safeWrite;
    }

    /**
     * Returns the number of maximum elements which are held in maximum memory.
     *
     * @return the number of elements.
     */
    public int getMaxBufferElements() {
        return maxBufferElements;
    }

    /**
     * Set maximum elements for the buffer. Change the value doesn't invoke a
     * trim call if the buffer contains more elements. Only write operations
     * force a trim call.
     *
     * @param maxBufferElements number of maximum elements.
     */
    public void setMaxBufferElements(int maxBufferElements) {
        this.maxBufferElements = maxBufferElements;
    }

    /**
     * Security check mostly copied from {@link InputStream#read(byte[], int, int)}.
     * Ensures the parameter are valid.
     * @param b the byte array to copy from
     * @param off the offset to read from the array
     * @param len the len of the bytes to read from the array
     * @return <code>true</code> if there are bytes to read, otherwise <code>false</code>
     * @throws NullPointerException if the array is null
     * @throws IndexOutOfBoundsException if the index is not correct
     */
    public static boolean correctOffsetAndLengthToRead(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return false;
        }
        return true;
    }

    /**
     * Security check mostly copied from {@link OutputStream#write(byte[], int, int)}.
     * Ensures the parameter are valid.
     * @param b the byte array to write to the array
     * @param off the offset to write to the array
     * @param len the len of the bytes to write to the array
     * @return <code>true</code> if there are bytes to write, otherwise <code>false</code>
     * @throws NullPointerException if the array is null
     * @throws IndexOutOfBoundsException if the index is not correct
     */
    public static boolean correctOffsetAndLengthToWrite(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0)
                || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return false;
        }
        return true;
    }

    /**
     * Call this method always after all changes are synchronized. This method
     * signals a modification. It cloud be a write on the stream or a close.
     */
    private void signalModification() {
        // hold up the permits to a maximum of one
        if (signalModification.availablePermits() == 0) {
            signalModification.release();
        }
        // same twice for external communication
        if (signalModificationExternal.availablePermits() == 0) {
            signalModificationExternal.release();
        }
    }

    /**
     * This method is blocking until data is available on the
     * {@link InputStream} or the stream was closed. This method could be called
     * from one thread only. This method could not be used to notify two threads.
     * @throws java.lang.InterruptedException if the current thread is interrupted
     */
    public void blockDataAvailable() throws InterruptedException {
        if (isClosed()) {
            return;
        }

        if (availableBytes < 1) {
            signalModificationExternal.acquire();
        }
    }

    /**
     * This method trims the buffer. This method can be invoked after every
     * write operation. The method checks itself if the buffer should be trimmed
     * or not.
     */
    private void trim() throws IOException {
        if (isTrimShouldBeExecuted()) {

            /**
             * Need to store more bufs, may it is not possible to read out all
             * data at once. The available method only returns an int value
             * instead a long value. Store all read parts of the full buffer in
             * a deque.
             */
            final Deque<byte[]> tmpBuffer = new LinkedList<>();

            int available;
            // empty the current buffer, read out all bytes
            while ((available = is.available()) > 0) {
                final byte[] buf = new byte[available];
                // read out of the buffer
                // and store the result to the tmpBuffer
                int read = is.read(buf);
                // should never happen
                assert read == available : "Read not enough bytes from buffer.";
                tmpBuffer.add(buf);
            }
            /**
             * Write all previously read parts back to the buffer. The buffer is
             * clean and contains no elements because all parts are read out.
             */
            try {
                /**
                 * The reference to the buffer deque is not reachable out of the
                 * trim method. It is possible to ignore the safeWrite flag to
                 * prevent not necessary clone operations.
                 */
                ignoreSafeWrite = true;
                while (!tmpBuffer.isEmpty()) {
                    // pollFirst returns always a non null value, tmpBuffer is only filled with non null values
                    os.write(tmpBuffer.pollFirst());
                }
            } finally {
                ignoreSafeWrite = false;
            }
        }
    }

    /**
     * Checks if a trim should be performed.
     * @return <code>true</code> if a trim should be performed, otherwise <code>false</code>.
     */
    private boolean isTrimShouldBeExecuted() {
        /**
         * To be thread safe, cache the maxBufferElements value. May the method
         * {@link #setMaxBufferElements(int)} was invoked from outside by another thread.
         */
        final int maxBufferElements = getMaxBufferElements();
        return (maxBufferElements > 0) && (buffer.size() >= 2) && (buffer.size() > maxBufferElements);
    }

    /**
     * This method mustn't be called in a synchronized context, the variable is
     * volatile.
     */
    private void requireNonClosed() throws IOException {
        if (streamClosed) {
            throw new IOException("Stream closed.");
        }
    }

    /**
     * This method blocks until the stream is closed or enough bytes are
     * available, which can be read from the buffer.
     *
     * @param bytes the number of bytes waiting for.
     * @throws IOException If the thread is interrupted.
     * @return The available bytes.
     */
    private long tryWaitForEnoughBytes(final long bytes) throws IOException {
        // we can only wait for a positive number of bytes
        assert bytes > 0 : "Number of bytes are negative or zero : " + bytes;

        // if we haven't enough bytes, the loop starts and wait for enough bytes
        while (bytes > availableBytes) {
            try {
                // first of all, check for a closed stream
                if (streamClosed) {
                    // is the stream closed, return only the current available bytes
                    return availableBytes;
                }
                // wait for a next loop run and block until a modification is signalized
                signalModification.acquire();
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }
        // return the available bytes (maybe higher as the required bytes)
        return availableBytes;
    }

    public class SBInputStream extends InputStream {
        @Override
        public int available() throws IOException {
            if (availableBytes > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) availableBytes;
        }

        @Override
        public void close() throws IOException {
            closeAll();
        }

        @Override
        public int read() throws IOException {
            // we wait for enough bytes (one byte)
            if (tryWaitForEnoughBytes(1) < 1) {
                // try to wait, but not enough bytes available
                // return the end of stream is reached
                return -1;
            }

            // enough bytes are available, lock and modify the FIFO
            synchronized (bufferLock) {
                // get the first element from FIFO
                final byte[] first = buffer.getFirst();
                // get the first byte
                byte value = first[positionAtCurrentBufferEntry];
                // we have the first byte, now set the pointer to the next value
                ++positionAtCurrentBufferEntry;
                // if the pointer was pointed to the last element of the
                // byte array remove the first element from FIFO and reset the pointer
                if (positionAtCurrentBufferEntry >= first.length) {
                    // reset the pointer
                    positionAtCurrentBufferEntry = 0;
                    // remove the first element from the buffer
                    buffer.pollFirst();
                }
                availableBytes--;
                // returned as int in the range 0 to 255.
                return value & 0xff;
            }
        }

        // please do not override the method "int read(byte b[])"
        // the method calls internal "read(b, 0, b.length)"
        @Override
        public int read(final byte b[], final int off, final int len) throws IOException {
            if (!correctOffsetAndLengthToRead(b, off, len)) {
                return 0;
            }

            // try to read the first byte from FIFO
            // copied from super.read
            // === snip
            int c = read();
            if (c == -1) {
                return -1;
            }
            b[off] = (byte) c;
            // === snap

            // we have already copied one byte, initialize with 1
            int copiedBytes = 1;

            int missingBytes = len - copiedBytes;
            if (noMoreMissingBytes(missingBytes)) {
                return copiedBytes;
            }

            long maximumAvailableBytes = tryWaitForEnoughBytes(missingBytes);

            if (maximumAvailableBytes < 1) {
                // try to wait, but no more bytes available
                return copiedBytes;
            }

            // some or enough bytes are available, lock and modify the FIFO
            synchronized (bufferLock) {
                for (;;) {

                    if (noMoreMissingBytes(missingBytes)) {
                        return copiedBytes;
                    }

                    // get the first element from FIFO
                    final byte[] first = buffer.getFirst();
                    // get the maximum bytes which can be copied
                    // from the first element
                    final int maximumBytesToCopy
                            = first.length - positionAtCurrentBufferEntry;

                    // this element can be copied fully to the destination
                    if (missingBytes >= maximumBytesToCopy) {
                        // copy the complete byte[] to the destination
                        System.arraycopy(first, positionAtCurrentBufferEntry, b,
                                copiedBytes + off, maximumBytesToCopy);
                        copiedBytes += maximumBytesToCopy;
                        maximumAvailableBytes -= maximumBytesToCopy;
                        availableBytes -= maximumBytesToCopy;
                        missingBytes -= maximumBytesToCopy;
                        // remove the first element from the buffer
                        buffer.pollFirst();
                        // reset the pointer
                        positionAtCurrentBufferEntry = 0;
                    } else {
                        // copy only a part of byte[] to the destination
                        System.arraycopy(first, positionAtCurrentBufferEntry, b,
                                copiedBytes + off, missingBytes);
                        // add the offset
                        positionAtCurrentBufferEntry += missingBytes;
                        copiedBytes += missingBytes;
                        maximumAvailableBytes -= missingBytes;
                        availableBytes -= missingBytes;
                        // set missing bytes to zero
                        // we reach the end of the current buffer (b)
                        missingBytes = 0;
                    }
                }
            }
        }

        /**
         * Ensure that no more bytes are missing.
         * @param missingBytes number of missing bytes.
         * @return <code>true</code> if no more bytes are missing, otherwise <code>false</code>.
         */
        private boolean noMoreMissingBytes(int missingBytes) {
            assert missingBytes >= 0 : "Copied more bytes as given";

            // check if we don't need to copy further bytes anymore
            return missingBytes == 0;
        }

    }

    public class SBOutputStream extends OutputStream {
        @Override
        public void close() throws IOException {
            closeAll();
        }

        @Override
        public void write(final int b) throws IOException {
            requireNonClosed();
            synchronized (bufferLock) {
                // add the byte to the buffer
                buffer.add(new byte[]{(byte) b});
                // increment the length
                ++availableBytes;
                // the count must be positive after any write operation
                assert availableBytes > 0 : "More memory used as a long can count";
                trim();
            }
            // always at least, signal bytes are written to the buffer
            signalModification();
        }

        // please do not override the method "void write(final byte[] b)"
        // the method calls internal "write(b, 0, b.length);"
        @Override
        public void write(final byte[] b, final int off, final int len)
                throws IOException {
            if (!correctOffsetAndLengthToWrite(b, off, len)) {
                return;
            }
            requireNonClosed();
            // To be thread safe cache the safeWrite value.
            boolean tmpSafeWrite = isSafeWrite();

            synchronized (bufferLock) {
                if (off == 0 && b.length == len) {
                    // add the full byte[] to the buffer
                    if (tmpSafeWrite && !ignoreSafeWrite) {
                        buffer.add(b.clone());
                    } else {
                        buffer.add(b);
                    }
                } else {
                    byte[] target = new byte[len];
                    System.arraycopy(b, off, target, 0, len);
                    buffer.add(target);
                }
                // increment the length
                availableBytes += len;
                // the count must be positive after any write operation
                assert availableBytes > 0 : "More memory used as a long can count";
                trim();
            }
            // always at least, signal bytes are written to the buffer
            signalModification();
        }
    }

    public void close() throws IOException {
        closeAll();
    }

    /**
     * Invoke a close.
     */
    private void closeAll() {
        streamClosed = true;
        signalModification();
    }

    /**
     * Returns <code>true</code> if the stream is closed. Otherwise
     * <code>false</code>.
     *
     * @return <code>true</code> if the stream is closed. Otherwise
     * <code>false</code>.
     */
    public boolean isClosed() {
        return streamClosed;
    }

    /**
     * Returns an input stream for this buffer.
     *
     * <p>
     * Under abnormal conditions the underlying buffer may be closed. When the
     * buffer is closed the following applies to the returned input stream :-
     *
     * <ul>
     *
     * <li><p>
     * If there are no bytes buffered on the buffer, and the buffer has not been
     * closed using {@link #close close}, then
     * {@link java.io.InputStream#available available} will return
     * <code>0</code>.
     *
     * </ul>
     *
     * <p>
     * Closing the returned {@link java.io.InputStream InputStream} will close
     * the complete buffer.
     *
     * @return an input stream for reading bytes from this buffer.
     */
    public InputStream getInputStream() {
        return is;
    }

    /**
     * Returns an output stream for this buffer.
     *
     * <p>
     * Closing the returned {@link java.io.OutputStream OutputStream} will close
     * the associated buffer.
     *
     * @return an output stream for writing bytes to this buffer.
     */
    public OutputStream getOutputStream() {
        return os;
    }

    /**
     * Returns the number of elements in the buffer.
     *
     * @return the number of elements in the buffer.
     */
    public int getBufferSize() {
        synchronized (bufferLock) {
            return buffer.size();
        }
    }
}
