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

package org.apache.airavata.mft.core.api;

import java.io.IOException;
import java.nio.channels.Channel;

/**
 * An interface represents a  channel of a {@Link Connector}
 */
public interface ConnectorChannel {
    /**
     * get the NIO channel of ChannelConnector
     *
     * @return
     */
    Channel getChannel();

    /**
     * close the channel if open else return
     *
     * @throws IOException
     */
    void closeChannel() throws IOException;

    /**
     * Save channel attribute to ConnectorChannel context
     *
     * @param key
     * @param value
     */
    void addChannelAttribute(String key, Object value);

    /**
     * Get channel attribute from ConnectorChannel context
     *
     * @param key
     * @return
     */
    Object getChannelAttribute(String key);

    /**
     * provides the initiated connector
     * @return Connector
     */
    Connector getSourceConnector();
}
