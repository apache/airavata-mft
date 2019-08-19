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
import org.apache.airavata.mft.core.api.ConnectorChannel;

import java.util.HashMap;

/**
 * A class to implement common method of In and Out Channels
 */
public abstract class AbstractChannel implements ConnectorChannel {

    private HashMap<String, Object> contextAttributeMap = new HashMap<>();

    private Connector myConnector;

    public AbstractChannel(Connector myConnector) {
        this.myConnector = myConnector;
    }

    @Override
    public void addChannelAttribute(String key, Object value) {
        contextAttributeMap.put(key, value);
    }

    @Override
    public Object getChannelAttribute(String key) {
        return contextAttributeMap.get(key);
    }

    @Override
    public Connector getSourceConnector() {
        return myConnector;
    }
}
