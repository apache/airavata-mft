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

package org.apache.airavata.mft.transport.tcp.client;

import org.apache.airavata.mft.core.api.ConnectorChannel;
import org.apache.airavata.mft.core.api.SinkConnector;
import org.apache.airavata.mft.core.bufferedImpl.channel.AbstractConnector;
import org.apache.airavata.mft.core.bufferedImpl.channel.OutChannel;
import org.apache.airavata.mft.transport.tcp.RemoteResourceIdentifier;

import java.nio.channels.SocketChannel;

/**
 * A class which represents the  connections between remote server and local client
 */
public class RemoteFileServerConnector extends AbstractConnector implements SinkConnector {

   private  RemoteResourceIdentifier identifier;

    public RemoteFileServerConnector(RemoteResourceIdentifier remoteResourceIdentifier) {
        this.identifier = remoteResourceIdentifier;
    }


    @Override
    public boolean verifyUpload(ConnectorChannel channel) {
        //TODO Implement this later
        return true;
    }

    @Override
    public ConnectorChannel openChannel() throws Exception {
        SocketChannel socketChannel = SocketChannel.open(this.identifier.getAddress());
        OutChannel outChannel = new OutChannel(socketChannel, this);
        return outChannel;
    }
}
