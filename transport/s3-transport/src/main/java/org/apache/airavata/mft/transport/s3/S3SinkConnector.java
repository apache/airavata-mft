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
import org.apache.airavata.mft.core.bufferedImpl.AbstractConnector;
import org.apache.airavata.mft.core.bufferedImpl.ConnectorConfig;
import org.apache.airavata.mft.core.bufferedImpl.Constants;
import org.apache.airavata.mft.core.bufferedImpl.OutChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channel;
import java.util.Properties;

/**
 * Connector class which connects to a given S3 destination and provides
 * S3 output Stream.
 */
public class S3SinkConnector extends AbstractConnector implements SinkConnector {
    private AmazonS3 s3Client;


    @Override
    public boolean initiate(ConnectorConfig connectorConfig) {
        s3Client = S3TransportUtil.getS3Client(connectorConfig.getValue(S3Constants.ACCESS_KEY),
                connectorConfig.getValue(S3Constants.SECRET_KEY), connectorConfig.getValue(S3Constants.REGION));
        return true;
    }

    @Override
    public ConnectorChannel openChannel(Properties properties) throws IOException {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += S3Constants.CONNECTION_EXPIRE_TIME;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest
                (properties.getProperty(S3Constants.BUCKET), properties.getProperty(S3Constants.REMOTE_FILE))
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
        cacheChannel(outChannel.getChannel(), outChannel);
        return outChannel;
    }

    @Override
    public boolean verifyUpload(Channel channel) {
        OutChannel outChannel = (OutChannel) getConnectorChannel(channel);
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
