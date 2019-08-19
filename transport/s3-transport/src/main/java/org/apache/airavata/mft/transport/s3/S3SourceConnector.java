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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.airavata.mft.core.api.ConnectorChannel;
import org.apache.airavata.mft.core.api.SourceConnector;
import org.apache.airavata.mft.core.bufferedImpl.Constants;
import org.apache.airavata.mft.core.bufferedImpl.channel.AbstractConnector;
import org.apache.airavata.mft.core.bufferedImpl.channel.InChannel;

import java.io.InputStream;

/**
 * Connector class which connects to a given S3 source and provides
 * S3 Input Stream.
 */
public class S3SourceConnector extends AbstractConnector implements SourceConnector {

    private AmazonS3 s3Client;

    private S3ResourceIdentifier identifier;

    public S3SourceConnector(S3ResourceIdentifier identifier) {
        this.identifier = identifier;
        this.s3Client = S3TransportUtil.getS3Client(identifier.getAccessKey(),
                identifier.getSecretKey(), identifier.getAccessKey());
    }

    @Override
    public ConnectorChannel openChannel() {
        S3Object s3object = s3Client.getObject(identifier.getBucket(),
                identifier.getRemoteFile());
        InputStream inputStream;
        if (s3object != null && s3object.getObjectContent() != null) {
            inputStream = s3object.getObjectContent();
            InChannel inChannel = new InChannel(inputStream);
            inChannel.addChannelAttribute(Constants.CONNECTOR, this);
            return inChannel;
        }
        return null;
    }


}
