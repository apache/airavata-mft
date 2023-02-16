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
import org.apache.airavata.mft.core.api.OutgoingChunkedConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class LocalOutgoingChunkedConnector implements OutgoingChunkedConnector {

    private String resourcePath;

    private static final Logger logger = LoggerFactory.getLogger(LocalOutgoingChunkedConnector.class);


    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.resourcePath = connectorConfig.getResourcePath();
    }

    @Override
    public void complete() throws Exception {
        logger.info("File {} successfully transferred", this.resourcePath);
    }

    @Override
    public void failed() throws Exception {
        logger.error("Failed while transferring file {}", this.resourcePath);
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, String uploadFile) throws Exception {

        FileInputStream from = new FileInputStream(new File(uploadFile));
        FileOutputStream to = new FileOutputStream(new File(this.resourcePath));

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
    public void uploadChunk(int chunkId, long startByte, long endByte, InputStream inputStream) throws Exception {


        FileOutputStream outputStream = new FileOutputStream(new File(this.resourcePath));

        byte[] buffer = new byte[1024];
        int bytesRead;
        inputStream.skip(startByte);
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
    }
}
