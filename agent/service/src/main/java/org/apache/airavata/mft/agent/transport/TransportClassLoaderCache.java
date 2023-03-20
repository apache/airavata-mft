/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.agent.transport;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransportClassLoaderCache {
    private final Map<String, TransportClassLoader> transportLoaderMap = new ConcurrentHashMap<>();
    private final String transportDirectory;

    public TransportClassLoaderCache(String transportDirectory) {
        this.transportDirectory = transportDirectory;
    }

    public TransportClassLoader fetchClassLoader(String transportName) throws IOException {
        if (!transportLoaderMap.containsKey(transportName)) {
            synchronized (this) {
                transportLoaderMap.put(transportName, new TransportClassLoader(
                        new URL[]{},
                        this.getClass().getClassLoader(),
                        Paths.get(transportDirectory, transportName + "-transport-bin.zip")));
            }
        }
        return transportLoaderMap.get(transportName);
    }
}
