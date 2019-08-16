/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.apache.airavata.mft.core.bufferedImpl;

import org.apache.airavata.mft.core.api.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * This class wire the input stream and output stream based on the Channel type.
 * If  either source or destination channels are FileChannel then wire using
 * zero copy technique of FileChannel class.
 * Other wise use DirectByteBuffers to copy data from source to destination.
 */
public class PassthroughMediator implements Mediator {

    @Override
    public void mediate(ConnectorChannel src, ConnectorChannel dst, CompletionCallback callback) {
        ReadableByteChannel rChannel = (ReadableByteChannel) src.getChannel();
        WritableByteChannel dChannel = (WritableByteChannel) dst.getChannel();
        try {
            if (rChannel instanceof FileChannel) {
                ChannelUtils.transferTo(dChannel, (FileChannel) src);
            } else if (dChannel instanceof FileChannel) {
                ChannelUtils.transferFrom(rChannel, (FileChannel) dst);
            } else {
                ChannelUtils.copyData(rChannel, dChannel);
            }


            Object obj = dst.getChannelAttribute(Constants.CONNECTOR);
            if (obj != null && obj instanceof SinkConnector) {
                SinkConnector connector = (SinkConnector) obj;
                boolean success = connector.verifyUpload(dChannel);
                if (success) {
                    callback.onComplete("Successfully uploaded", null);
                } else {
                    String msg = "Upload Failed";
                    ConnectorException connectorException = new ConnectorException(msg, null);
                    callback.onComplete("Upload failed ", connectorException);
                }
            }

        } catch (IOException e) {
            String msg = "Upload Failed";
            ConnectorException connectorException = new ConnectorException(msg, e);
            callback.onComplete("Upload failed ", connectorException);
        } finally {
            Connector sourceConnector = (Connector) src.getChannelAttribute(Constants.CONNECTOR);
            Connector sinkConnector = (Connector) dst.getChannelAttribute(Constants.CONNECTOR);
            try {
                sourceConnector.closeChannel(rChannel);
                sinkConnector.closeChannel(dChannel);
            } catch (Exception ex) {
                String msg = "Error occurred while closing channels";
                ConnectorException connectorException = new ConnectorException(msg, ex);
                callback.onComplete(msg, connectorException);
            }
        }

    }
}
