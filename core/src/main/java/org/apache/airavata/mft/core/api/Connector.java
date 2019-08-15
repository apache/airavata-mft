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

import org.apache.airavata.mft.core.bufferedImpl.ConnectorConfig;

import java.nio.channels.Channel;
import java.util.Properties;

/**
 * This represents a connection between external source or sink
 */
public interface Connector {

    /**
     * Initiates the connector object
     * @param connectorConfig
     * @return initation state whether success or not
     */
    boolean initiate(ConnectorConfig connectorConfig);

    /**
     * This returns a {@link ConnectorChannel}
     * @return Channel
     */
    ConnectorChannel openChannel(Properties properties) throws Exception;

    /**
     * This is used to close the channel and release resources related to channel
     * @param channel
     * @throws Exception
     */
    void closeChannel(Channel channel) throws  Exception;
}
