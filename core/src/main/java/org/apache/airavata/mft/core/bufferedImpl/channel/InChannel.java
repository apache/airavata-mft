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

import org.apache.airavata.mft.core.api.ConnectorChannel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

/**
 * A class which represents the channel of a {@Link SinkConnector}
 */
public class InChannel implements ConnectorChannel {

    private InputStream inputStream;

    private ReadableByteChannel readableByteChannel;

    private HashMap<String, Object> contextAttributeMap = new HashMap<>();

    public InChannel(InputStream inputStream) {
        this.inputStream = inputStream;
        readableByteChannel = Channels.newChannel(inputStream);

    }


    @Override
    public Channel getChannel() {
        return readableByteChannel;
    }

    @Override
    public void closeChannel() throws IOException {
        inputStream.close();
    }

    @Override
    public void addChannelAttribute(String key, Object value) {
        contextAttributeMap.put(key, value);
    }

    @Override
    public Object getChannelAttribute(String key) {
        return contextAttributeMap.get(key);
    }
}
