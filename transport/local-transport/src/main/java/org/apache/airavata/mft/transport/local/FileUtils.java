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

import org.apache.airavata.mft.core.bufferedImpl.ConnectorException;

import java.io.File;

/**
 * A class having utility methods of local file manipulation
 */
public class FileUtils {

    /**
     * Validates and provides the file
     * @param src
     * @return
     * @throws ConnectorException
     */
    public static File getFile(String src) throws ConnectorException {
        if (src != null) {
            final File file = new File(src);
            if (file.isFile()) {
                return file;
            } else {
                throw new ConnectorException("File " + src + " is not a  valid file", null);
            }
        } else {
            throw new ConnectorException("Please specify validate file name", null);
        }

    }


}
