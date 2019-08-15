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

package org.apache.airavata.mft.core.bufferedImpl;

import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.SinkConnector;
import org.apache.airavata.mft.core.api.SourceConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common class for {@link SinkConnector} and {@link SourceConnector} implementations
 * This overrides closeChannel method and release all resources allocated to releasing channel
 */
public abstract class AbstractConnector implements Connector {
    ConcurrentHashMap<Channel, Object> channelToStreamMap = new ConcurrentHashMap<>();

    @Override
    public void closeChannel(Channel channel) throws ConnectorException {
        OutChannel outChannel;
        InChannel inChannel;

        try {
            if (channelToStreamMap.get(channel) instanceof OutputStream) {
                outChannel = (OutChannel) channelToStreamMap.get(channel);
                if (outChannel != null) {
                    outChannel.closeChannel();
                    System.out.println("Closing output stream");
                }

            } else if (channelToStreamMap.get(channel) instanceof InputStream) {
                inChannel = (InChannel) channelToStreamMap.get(channel);
                if (inChannel != null) {
                    inChannel.closeChannel();
                }
            }

        } catch (IOException e) {
            throw new ConnectorException("Error occurred while closing stream", e);
        }

    }

    public void cacheChannel(Channel channel, Object obj) {
        channelToStreamMap.put(channel, obj);
    }


    public Object getConnectorChannel(Channel channel) {
       return channelToStreamMap.get(channel);
    }


}
