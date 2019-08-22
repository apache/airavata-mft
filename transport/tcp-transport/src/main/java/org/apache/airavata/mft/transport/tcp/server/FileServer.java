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

package org.apache.airavata.mft.transport.tcp.server;

import com.sun.istack.internal.Nullable;
import org.apache.airavata.mft.transport.tcp.Constants;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class which is responsible for accepting incoming tcp connections
 * and delegate accepted channels to worker objects
 */
public class FileServer {


    //If too many  host address
    private ConcurrentHashMap<String, FileServerConnector> remoteHostToConnectorMap = new ConcurrentHashMap<>();

    private volatile boolean runServer = true;

    /**
     * This starts the server and  accepts incoming connections
     *
     * @return
     */
    public void start(int port) {
        try {
            remoteHostToConnectorMap.put(Constants.UNKNOWN_HOST, new FileServerConnector());
            while (runServer) {
                final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                //run while loop without waiting for connections
                serverSocketChannel.configureBlocking(false);

                SocketChannel channel = serverSocketChannel.accept();

                if (channel != null) {
                    InetSocketAddress socketAddress = (InetSocketAddress) channel.getRemoteAddress();

                    String hostName = socketAddress.getHostName();

                    FileServerConnector fileServerConnector = remoteHostToConnectorMap.get(hostName);

                    if (fileServerConnector == null) {
                        fileServerConnector = new FileServerConnector();
                        remoteHostToConnectorMap.put(hostName, fileServerConnector);
                    }

                    fileServerConnector.addChannel(channel);


                }
            }
        } catch (Exception ex) {
            //TODO: log exception
        }
    }

    /**
     * Server is shutdown and stops  accepting incoming connections
     *
     * @return
     */
    public void stop() {
        this.runServer = false;
    }


    /**
     * Get connector bound to remote client. If remote host is not known keep it null.
     *
     * @param host
     * @return
     */
    public FileServerConnector getConnector(@Nullable String host) {
        FileServerConnector fileServerConnector = null;
        if (host != null) {
            fileServerConnector = remoteHostToConnectorMap.get(host);
            if (fileServerConnector == null) {
                fileServerConnector = new FileServerConnector();
                remoteHostToConnectorMap.put(host, fileServerConnector);
            }
        } else {
            fileServerConnector = remoteHostToConnectorMap.get(Constants.UNKNOWN_HOST);

        }
        return fileServerConnector;
    }


}
