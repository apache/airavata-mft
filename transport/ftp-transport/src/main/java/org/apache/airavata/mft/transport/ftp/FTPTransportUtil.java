package org.apache.airavata.mft.transport.ftp;

import org.apache.airavata.mft.resource.service.FTPResource;
import org.apache.airavata.mft.secret.service.FTPSecret;
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
