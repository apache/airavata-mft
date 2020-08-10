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

package org.apache.airavata.mft.transport.ftp;

import org.apache.airavata.mft.credential.stubs.ftp.FTPSecret;
import org.apache.airavata.mft.resource.stubs.ftp.resource.FTPResource;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class FTPTransportUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPTransportUtil.class);

    static FTPClient getFTPClient(FTPResource ftpResource, FTPSecret ftpSecret) throws IOException {

        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(ftpResource.getFtpStorage().getHost(), ftpResource.getFtpStorage().getPort());
        ftpClient.enterLocalActiveMode();
        ftpClient.login(ftpSecret.getUserId(), ftpSecret.getPassword());

        return ftpClient;
    }

    static void disconnectFTP(FTPClient ftpClient) {
        try {
            if (ftpClient != null) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            logger.error("FTP client close operation failed", e);
        }
    }
}
