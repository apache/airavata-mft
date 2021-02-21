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

import org.apache.airavata.mft.core.AuthZToken;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class LocalSender implements Connector {

    private static final Logger logger = LoggerFactory.getLogger(LocalSender.class);

    private boolean initialized;
    @Override
    public void init(AuthZToken authZToken, String storageId, String credentialToken, String resourceServiceHost, int resourceServicePort,
                     String secretServiceHost, int secretServicePort) throws Exception {

        this.initialized = true;
    }


    @Override
    public void destroy() {

    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Local Sender is not initialized");
        }
    }

    @Override
    public void startStream(String targetPath, ConnectorContext context) throws Exception {

        logger.info("Starting local sender stream for transfer {}", context.getTransferId());

        checkInitialized();

        InputStream in = context.getStreamBuffer().getInputStream();
        long fileSize = context.getMetadata().getResourceSize();
        OutputStream fos = new FileOutputStream(targetPath);

        byte[] buf = new byte[1024];
        while (true) {
            int bufSize = 0;

            if (buf.length < fileSize) {
                bufSize = buf.length;
            } else {
                bufSize = (int) fileSize;
            }
            bufSize = in.read(buf, 0, bufSize);

            if (bufSize < 0) {
                break;
            }

            fos.write(buf, 0, bufSize);
            fos.flush();

            fileSize -= bufSize;
            if (fileSize == 0L)
                break;
        }

        in.close();
        fos.close();

        logger.info("Completed local sender stream for transfer {}", context.getTransferId());
    }
}
