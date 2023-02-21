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
import org.apache.airavata.mft.core.api.IncomingChunkedConnector;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalIncomingChunkedConnector implements IncomingChunkedConnector {

    private String resourcePath;

    private static final Logger logger = LoggerFactory.getLogger(LocalIncomingChunkedConnector.class);

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
    public void downloadChunk(int chunkId, long startByte, long endByte, String downloadFile) throws Exception {

        FileInputStream from = new FileInputStream(new File(this.resourcePath));
        FileOutputStream to = new FileOutputStream(new File(downloadFile));

        final int buffLen = 1024;

        byte[] buf = new byte[buffLen];

        from.skip(startByte);

        long fileSize = endByte - startByte + 1;

        while (true) {
            int bufSize = 0;

            if (buffLen < fileSize) {
                bufSize = buffLen;
            } else {
                bufSize = (int) fileSize;
            }

            bufSize = (int) from.read(buf, 0, bufSize);

            if (bufSize < 0) {
                break;
            }

            to.write(buf, 0, bufSize);
            to.flush();

            fileSize -= bufSize;

            if (fileSize == 0L) {
                break;
            }
        }

        from.close();
        to.close();

    }

    @Override
    public InputStream downloadChunk(int chunkId, long startByte, long endByte) throws Exception {

        FileInputStream from = new FileInputStream(new File(this.resourcePath));

        from.skip(startByte);

        return from;
    }
}
