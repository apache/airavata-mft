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

import java.io.IOException;
import java.io.InputStream;

/**
 * This accepts {@link DoubleByteArrayOutputStream} and monitors for the availability of readable data. If there is any,
 * this is exposing them as an {@link InputStream}
 */
public class DoubleByteArrayInputStream extends InputStream {

    private DoubleByteArrayOutputStream outputStream;
    private InputStream currentInputStream;

    public DoubleByteArrayInputStream(DoubleByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
        this.currentInputStream = outputStream.asInputStream();
    }

    @Override
    public int read() throws IOException {
        refresh();
        return this.currentInputStream.read();
    }

    @Override
    public int available() throws IOException {
        refresh();
        return this.currentInputStream.available();
    }

    /**
     * This will check if there is new data in the {@link DoubleByteArrayOutputStream}
     * @throws IOException
     */
    private void refresh() throws IOException {
        if (this.currentInputStream.available() == 0) {
            InputStream tempInputStream = this.outputStream.asInputStream();
            if (tempInputStream != null) {
                this.currentInputStream = tempInputStream;
            }
        }
    }
}
