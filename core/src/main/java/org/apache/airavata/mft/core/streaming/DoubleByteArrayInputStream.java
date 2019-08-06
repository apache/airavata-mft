package org.apache.airavata.mft.core.streaming;

import java.io.IOException;
import java.io.InputStream;

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

    private void refresh() throws IOException {
        if (this.currentInputStream.available() == 0) {
            InputStream tempInputStream = this.outputStream.asInputStream();
            if (tempInputStream != null) {
                this.currentInputStream = tempInputStream;
            }
        }
    }
}
