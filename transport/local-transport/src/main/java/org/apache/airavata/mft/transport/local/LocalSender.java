package org.apache.airavata.mft.transport.local;

import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;

import java.io.FileOutputStream;
import java.io.InputStream;

public class LocalSender implements Connector {

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
        System.out.println("Starting local send");
        FileOutputStream fos = new FileOutputStream(this.resource.getPath());
        long fileSize = context.getMetadata().getResourceSize();

        InputStream inputStream = context.getStreamBuffer().getInputStream();

        byte[] buf = new byte[1];
        while (true) {
            int bufSize = 0;

            if (buf.length < fileSize) {
                bufSize = buf.length;
            } else {
                bufSize = (int) fileSize;
            }
            bufSize = inputStream.read(buf, 0, bufSize);

            if (bufSize < 0) {
                break;
            }

            fos.write(buf, 0, bufSize);
            fos.flush();

            fileSize -= bufSize;
            if (fileSize == 0L)
                break;
        }
        System.out.println("Completed local send");
    }
}
