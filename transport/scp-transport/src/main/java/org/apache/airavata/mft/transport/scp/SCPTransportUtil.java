package org.apache.airavata.mft.transport.scp;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Properties;

public class SCPTransportUtil {
    // TODO replace with an API call to the registry
    public static SSHResourceIdentifier getSSHResourceIdentifier(String resourceId) {

        SSHResourceIdentifier identifier = new SSHResourceIdentifier();
        switch (resourceId){
            case "1":
                identifier.setHost("pgadev.scigap.org");
                identifier.setUser("pga");
                identifier.setPort(22);
                identifier.setKeyFile("/Users/dwannipu/.ssh/id_rsa");
                identifier.setKeyPassphrase(null);
                identifier.setRemotePath("/var/www/portals/gateway-user-data/dev-seagrid/eromads6/DefaultProject/Gaussian_C11470169729/file.txt");
                return identifier;
            case "2":
                identifier.setHost("pgadev.scigap.org");
                identifier.setUser("pga");
                identifier.setPort(22);
                identifier.setKeyFile("/Users/dwannipu/.ssh/id_rsa");
                identifier.setKeyPassphrase(null);
                identifier.setRemotePath("/var/www/portals/gateway-user-data/dev-seagrid/eromads6/DefaultProject/Gaussian_C11470169729/new-file.txt");
                return identifier;
        }
        return null;
    }

    public static Session createSession(String user, String host, int port, String keyFilePath, String keyPassword) {
        try {
            JSch jsch = new JSch();

            if (keyFilePath != null) {
                if (keyPassword != null) {
                    jsch.addIdentity(keyFilePath, keyPassword);
                } else {
                    jsch.addIdentity(keyFilePath);
                }
            }

            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, host, port);
            session.setConfig(config);
            session.connect();

            return session;
        } catch (JSchException e) {
            System.out.println(e);
            return null;
        }
    }
}
