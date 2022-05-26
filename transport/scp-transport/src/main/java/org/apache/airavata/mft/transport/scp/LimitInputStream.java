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

package org.apache.airavata.mft.transport.scp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.util.concurrent.atomic.AtomicBoolean;

public class LimitInputStream extends FilterInputStream implements Channel {
    private final AtomicBoolean open = new AtomicBoolean(true);
    private long remaining;

    public LimitInputStream(InputStream in, long length) {
        super(in);
        this.remaining = length;
    }

    public boolean isOpen() {
        return this.open.get();
    }

    public int read() throws IOException {
        if (!this.isOpen()) {
            throw new IOException("read() - stream is closed (remaining=" + this.remaining + ")");
        } else if (this.remaining > 0L) {
            --this.remaining;
            return super.read();
        } else {
            return -1;
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (!this.isOpen()) {
            throw new IOException("read(len=" + len + ") stream is closed (remaining=" + this.remaining + ")");
        } else {
            int nb = len;
            if ((long)len > this.remaining) {
                nb = (int)this.remaining;
            }

            if (nb > 0) {
                int read = super.read(b, off, nb);
                this.remaining -= (long)read;
                return read;
            } else {
                return -1;
            }
        }
    }

    public long skip(long n) throws IOException {
        if (!this.isOpen()) {
            throw new IOException("skip(" + n + ") stream is closed (remaining=" + this.remaining + ")");
        } else {
            long skipped = super.skip(n);
            this.remaining -= skipped;
            return skipped;
        }
    }

    public int available() throws IOException {
        if (!this.isOpen()) {
            throw new IOException("available() stream is closed (remaining=" + this.remaining + ")");
        } else {
            int av = super.available();
            return (long)av > this.remaining ? (int)this.remaining : av;
        }
    }

    public void close() throws IOException {
        if (!this.open.getAndSet(false)) {
            ;
        }
    }
}

