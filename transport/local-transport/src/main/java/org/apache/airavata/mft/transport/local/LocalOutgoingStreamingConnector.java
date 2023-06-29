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
import org.apache.airavata.mft.core.api.OutgoingStreamingConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class LocalOutgoingStreamingConnector implements OutgoingStreamingConnector {

    private String resourcePath;

    private static final Logger logger = LoggerFactory.getLogger(LocalOutgoingStreamingConnector.class);

    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.resourcePath = connectorConfig.getResourcePath();
    }

    @Override
    public void complete() throws Exception {
        logger.info("File {} successfully written", this.resourcePath);
    }

    @Override
    public void failed() throws Exception {
        logger.error("Failed while writing file {}", this.resourcePath);
    }

    @Override
    public OutputStream fetchOutputStream() throws Exception {
        return new FileOutputStream(this.resourcePath);
    }
}
