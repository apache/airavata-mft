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

import java.io.*;

public class LocalOutgoingChunkedConnector implements OutgoingChunkedConnector {

    private String resourcePath;

    private long resourceSize;
    private boolean dmaFlag;

    private int buffLen;

    private static final Logger logger = LoggerFactory.getLogger(LocalOutgoingChunkedConnector.class);


    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.resourcePath = connectorConfig.getResourcePath();
        this.resourceSize = connectorConfig.getMetadata().getFile().getResourceSize();
        this.dmaFlag = connectorConfig.getBooleanTransportProperty(ConnectorConfig.LocalConfigs.DMA_ENABLED, false);
        this.buffLen = connectorConfig.getIntTransportProperty(ConnectorConfig.LocalConfigs.BUFF_LEN, 16 * 1024 * 1024);
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

        logger.info("Uploading chunk {} with start byte {} and end byte {} to file {} from upload file {}",
                chunkId, startByte, endByte, this.resourcePath, uploadFile);

        if (dmaFlag) {
            try {
                FileInputStream from = new FileInputStream(uploadFile);
                RandomAccessFile file = new RandomAccessFile(this.resourcePath, "rw");
                file.seek(startByte);
                FileOutputStream to = new FileOutputStream(file.getFD());
                from.getChannel().transferTo(0, endByte - startByte, to.getChannel());
                file.close();
                from.close();
                to.close();
            } catch (Exception e) {
                logger.error("Unexpected error occurred while uploading chunk {} to file {} from upload file {}",
                        chunkId, this.resourcePath, uploadFile, e);
                throw e;
            }
//            }
        }   else {
            try {
                RandomAccessFile file = new RandomAccessFile(this.resourcePath, "rw");
                file.seek(startByte);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getFD()));
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(uploadFile), buffLen);
                byte[] buffer = new byte[buffLen];
                int write = 0;
                long totalWritten = 0l;
                while ((write = bis.read(buffer, 0, Math.min(buffLen, (int) (endByte - totalWritten)))) > 0) {
                    bos.write(buffer, (int) 0, write);
                    totalWritten += write;
                }
                bis.close();
                bos.close();
                file.close();
            } catch (Exception e) {
                logger.error("Unexpected error occurred while uploading chunk {} to file {} from upload file {}",
                        chunkId, this.resourcePath, uploadFile,  e);
                throw e;
            }
        }
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, InputStream inputStream) throws Exception {

        logger.info("Uploading chunk {} with start byte {} and end byte {} to file {} from inputStream {}",
                chunkId, startByte, endByte, this.resourcePath, "test");

        RandomAccessFile file = new RandomAccessFile(this.resourcePath, "rw");
        file.seek(startByte);
        FileOutputStream outputStream = new FileOutputStream(file.getFD());

        byte[] buffer = new byte[buffLen];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
    }
}
