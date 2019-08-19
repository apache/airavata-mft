/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.apache.airavata.mft.core.bufferedImpl.channel;

import org.apache.airavata.mft.core.bufferedImpl.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Contains the utility methods for Channel Operations
 */
public class ChannelUtils {

    /**
     * Tranfer data from readable byte channel to FileChannel using zero-copy
     *
     * @param byteChannel
     * @param to
     * @throws IOException
     */
    public static void transferFrom(ReadableByteChannel byteChannel, FileChannel to) throws IOException {
        long count;
        long total = 0;
        while ((count = to.transferFrom(byteChannel, total, Constants.TRANSFER_MAX_SIZE)) > 0) {
            total = +count;
        }
    }

    /**
     * Transfer data from FileChannel to writable byte channel using zero-copy
     *
     * @param to
     * @param from
     * @throws IOException
     */
    public static void transferTo(WritableByteChannel to, FileChannel from) throws IOException {
        long count;
        long total = 0;
        while ((count = from.transferTo(total, Constants.TRANSFER_MAX_SIZE, to)) > 0) {
            total = +count;
        }
    }

    /**
     * Copy data from readable byte channel to writable byte channel
     *
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copyData(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(Constants.BUFFER_SIZE);
        int count = 0;
        while ((count = src.read(buffer)) != -1) {
            // prepare the buffer to be drained
            buffer.flip();
            // write to the channel, may block
            dest.write(buffer);
            // If partial transfer, shift remainder down
            // If buffer is empty, same as doing clear()
            buffer.compact();
        }
        // EOF will leave buffer in fill state
        buffer.flip();
        // make sure the buffer is fully drained.
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }

    }
}
