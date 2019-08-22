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

import org.apache.airavata.mft.core.api.Connector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * A class which represents the channel of a {@Link SinkConnector}
 */
public class InChannel extends AbstractChannel {

    private InputStream inputStream;

    private ReadableByteChannel readableByteChannel;


    public InChannel(InputStream inputStream, Connector sourceConnector) {
        super(sourceConnector);
        this.inputStream = inputStream;
        readableByteChannel = Channels.newChannel(inputStream);

    }

    public InChannel(ReadableByteChannel channel, Connector sourceConnector) {
        super(sourceConnector);
        readableByteChannel = channel;

    }


    @Override
    public Channel getChannel() {
        return readableByteChannel;
    }

    @Override
    public void closeChannel() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        } else if (readableByteChannel != null) {
            readableByteChannel.close();
        }
    }


}
