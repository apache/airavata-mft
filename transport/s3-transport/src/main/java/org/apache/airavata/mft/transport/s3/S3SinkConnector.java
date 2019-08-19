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

package org.apache.airavata.mft.transport.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import org.apache.airavata.mft.core.api.ConnectorChannel;
import org.apache.airavata.mft.core.api.SinkConnector;
import org.apache.airavata.mft.core.bufferedImpl.Constants;
import org.apache.airavata.mft.core.bufferedImpl.channel.AbstractConnector;
import org.apache.airavata.mft.core.bufferedImpl.channel.OutChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Connector class which connects to a given S3 destination and provides
 * S3 output Stream.
 */
public class S3SinkConnector extends AbstractConnector implements SinkConnector {
    private AmazonS3 s3Client;
    private S3ResourceIdentifier s3ResourceIdentifier;

    public S3SinkConnector(S3ResourceIdentifier s3ResourceIdentifier) {
        this.s3ResourceIdentifier = s3ResourceIdentifier;
        this.s3Client = S3TransportUtil.getS3Client(s3ResourceIdentifier.getAccessKey(),
                s3ResourceIdentifier.getSecretKey(), s3ResourceIdentifier.getRegion());
    }

    @Override
    public ConnectorChannel openChannel() throws IOException {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += S3Constants.CONNECTION_EXPIRE_TIME;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest
                (s3ResourceIdentifier.getBucket(), s3ResourceIdentifier.getRemoteFile())
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);
        URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        OutputStream outputStream = connection.getOutputStream();
        OutChannel outChannel = new OutChannel(outputStream);
        outChannel.addChannelAttribute(S3Constants.HTTP_CONNECTION, connection);
        outChannel.addChannelAttribute(Constants.CONNECTOR, this);
        return outChannel;
    }

    @Override
    public boolean verifyUpload(ConnectorChannel channel) {
        OutChannel outChannel = (OutChannel) channel;
        HttpURLConnection connection = (HttpURLConnection) outChannel.getChannelAttribute(S3Constants.HTTP_CONNECTION);
        try {
            if (connection.getResponseCode() == S3Constants.HTTP_SUCCESS_RESPONSE_CODE) {
                return true;
            } else {
                return false;
            }
        } catch (Exception error) {
            return false;
        }
    }


}
