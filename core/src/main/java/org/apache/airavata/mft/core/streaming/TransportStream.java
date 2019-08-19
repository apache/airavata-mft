/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.mft.core.streaming;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransportStream {

    private TransportMetadata metadata;
    private AtomicBoolean streamCompleted = new AtomicBoolean(false);

    private OutputStream outputStream = new DoubleByteArrayOutputStream();
    private DoubleByteArrayInputStream inputStream = new DoubleByteArrayInputStream((DoubleByteArrayOutputStream) outputStream);

    private String sourceId;
    private String destId;

    public TransportStream(String sourceId, String destId, TransportMetadata metadata) throws Exception {
        this.sourceId = sourceId;
        this.destId = destId;
        this.metadata = metadata;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public TransportMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TransportMetadata metadata) {
        this.metadata = metadata;
    }

    public Boolean isStreamCompleted() {
        return streamCompleted.get();
    }

    public void setStreamCompleted(boolean completed) throws IOException {
        this.outputStream.close();
        this.streamCompleted.set(completed);
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }
}
