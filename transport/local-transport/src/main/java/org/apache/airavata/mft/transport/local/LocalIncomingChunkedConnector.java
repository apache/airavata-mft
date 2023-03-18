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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalIncomingChunkedConnector implements IncomingChunkedConnector {

    private String resourcePath;
    private long resourceSize;

    private static final Logger logger = LoggerFactory.getLogger(LocalIncomingChunkedConnector.class);

    @Override
    public void init(ConnectorConfig connectorConfig) throws Exception {
        this.resourcePath = connectorConfig.getResourcePath();
        this.resourceSize = connectorConfig.getMetadata().getFile().getResourceSize();
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

        logger.info("Downloading chunk {} with start byte {} and end byte {} to file {} from resource path {}",
                chunkId, startByte, endByte, downloadFile, this.resourcePath);

//        #use this code on a DMA enabled device
//        if (resourceSize <= endByte - startByte) {
//            Files.copy(Path.of(this.resourcePath), Path.of(downloadFile));
//        } else {
//            try (FileInputStream from = new FileInputStream(this.resourcePath);
//                 FileOutputStream to = new FileOutputStream(downloadFile)) {
//                from.getChannel().transferTo(startByte, endByte - startByte, to.getChannel());
//            } catch (Exception e) {
//                logger.error("Unexpected error occurred while downloading chunk {} to file {} from resource path {}",
//                        chunkId, downloadFile, this.resourcePath, e);
//                throw e;
//            }
//        }

        int buffLen = 1024 * 1024 * 16;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(this.resourcePath),buffLen);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(downloadFile))) {
            byte[] buffer = new byte[buffLen];
            int read = 0;
            long totalRead = bis.skip(startByte);
            while ((read = bis.read(buffer,0,Math.min(buffLen, (int) (endByte - totalRead )))) > 0) {
                bos.write(buffer, 0, read);
                totalRead += read;
            }
            bis.close();
            bos.close();
        } catch (Exception e) {
            logger.error("Unexpected error occurred while downloading chunk {} to file {} from resource path {}",
                        chunkId, downloadFile, this.resourcePath, e);
                throw e;
        }
    }

    @Override
    public InputStream downloadChunk(int chunkId, long startByte, long endByte) throws Exception {

        FileInputStream from = new FileInputStream(new File(this.resourcePath));

        from.skip(startByte);

        return new BufferedInputStream(from, Math.min(16 * 1024 * 1024,(int) (endByte - startByte)));
    }
}
