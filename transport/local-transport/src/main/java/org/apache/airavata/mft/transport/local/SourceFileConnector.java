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

package org.apache.airavata.mft.transport.local;

import org.apache.airavata.mft.core.api.ConnectorChannel;
import org.apache.airavata.mft.core.api.SourceConnector;
import org.apache.airavata.mft.core.bufferedImpl.channel.AbstractConnector;
import org.apache.airavata.mft.core.bufferedImpl.channel.InChannel;

import java.io.File;
import java.io.FileInputStream;


/**
 * A class responsible for open up the connection to read from file
 */
public class SourceFileConnector extends AbstractConnector implements SourceConnector {

    private ResourceIdentifier resourceIdentifier;

    public SourceFileConnector(ResourceIdentifier resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    @Override
    public ConnectorChannel openChannel() throws Exception {
        File file = FileUtils.getFile(resourceIdentifier.getFilePath());
        FileInputStream inputStream = new FileInputStream(file);
        return new InChannel(inputStream, this);
    }


}
