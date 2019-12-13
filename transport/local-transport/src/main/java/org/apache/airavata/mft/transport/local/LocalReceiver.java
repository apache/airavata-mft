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

import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;

import java.io.FileInputStream;
import java.io.OutputStream;

public class LocalReceiver implements Connector {

    private LocalResourceIdentifier resource;
    @Override
    public void init(String resourceId, String credentialToken) throws Exception {
        this.resource = LocalTransportUtil.getLocalResourceIdentifier(resourceId);
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        System.out.println("Starting local receive");
        FileInputStream fin = new FileInputStream(this.resource.getPath());

        long fileSize = context.getMetadata().getResourceSize();

        OutputStream outputStream = context.getStreamBuffer().getOutputStream();

        byte[] buf = new byte[1024];
        while (true) {
            int bufSize = 0;

            if (buf.length < fileSize) {
                bufSize = buf.length;
            } else {
                bufSize = (int) fileSize;
            }
            bufSize = fin.read(buf, 0, bufSize);

            if (bufSize < 0) {
                break;
            }

            outputStream.write(buf, 0, bufSize);
            outputStream.flush();

            fileSize -= bufSize;
            if (fileSize == 0L)
                break;
        }

        fin.close();
        outputStream.close();
        //context.getStreamBuffer().close();
        System.out.println("Completed local receive");
    }
}
