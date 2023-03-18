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

package org.apache.airavata.mft.transport.local;


import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingStreamingConnector;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalIncomingStreamingConnector implements IncomingStreamingConnector{

    private String resourcePath;

    private static final Logger logger = LoggerFactory.getLogger(LocalIncomingStreamingConnector.class);

    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.resourcePath = connectorConfig.getResourcePath();
    }

    @Override
    public void complete() throws Exception {
        logger.info("File {} successfully received", this.resourcePath);
    }

    @Override
    public void failed() throws Exception {
        logger.error("Failed while receiving file {}", this.resourcePath);
    }

    @Override
    public InputStream fetchInputStream() throws Exception {
        InputStream from = new FileInputStream(new File(this.resourcePath));

        return from;
    }
}
