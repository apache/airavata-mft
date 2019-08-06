package org.apache.airavata.mft.transport.scp;

import com.jcraft.jsch.*;
import org.apache.airavata.mft.core.api.StreamedReceiver;
import org.apache.airavata.mft.core.streaming.TransportStream;

import java.io.*;
import java.util.Properties;

public class SCPReceiver implements StreamedReceiver {

    @Override
    public void receive(String resourceIdentifier, TransportStream stream) throws Exception {
        SSHResourceIdentifier sshResourceIdentifier = getSSHResourceIdentifier(resourceIdentifier);
        Session session = createSession(sshResourceIdentifier.getUser(), sshResourceIdentifier.getHost(),
                sshResourceIdentifier.getPort(),
                sshResourceIdentifier.getKeyFile(),
                sshResourceIdentifier.getKeyPassphrase());
        transferRemoteToStream(session, sshResourceIdentifier.getRemotePath(), stream);
    }

    // TODO replace with an API call to the registry
    private SSHResourceIdentifier getSSHResourceIdentifier(String resourceId) {
        SSHResourceIdentifier identifier = new SSHResourceIdentifier();
        identifier.setHost("pgadev.scigap.org");
        identifier.setUser("pga");
        identifier.setPort(22);
        identifier.setKeyFile("/Users/user/.ssh/id_rsa");
        identifier.setKeyPassphrase(null);
        identifier.setRemotePath("/var/www/portals/gateway-user-data/dev-seagrid/eromads6/DefaultProject/Gaussian_C11470169729/file.txt");
        return identifier;
    }

    private void transferRemoteToStream(Session session, String from, TransportStream stream) throws JSchException, IOException {

        // exec 'scp -f rfile' remotely
        String command = "scp -f " + from;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        byte[] buf = new byte[1024];

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        while (true) {
            int c = checkAck(in);
            if (c != 'C') {
                break;
            }

            // read '0644 '
            in.read(buf, 0, 5);

            long filesize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    break;
                }
                if (buf[0] == ' ') break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }

            String file = null;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }

            System.out.println("file-size=" + filesize + ", file=" + file);
            stream.setLength(filesize);
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of lfile
            int bufSize;
            while (true) {
                if (buf.length < filesize) bufSize = buf.length;
                else bufSize = (int) filesize;
                bufSize = in.read(buf, 0, bufSize);
                if (bufSize < 0) {
                    // error
                    break;
                }
                stream.getOutputStream().write(buf, 0, bufSize);
                stream.getOutputStream().flush();

                filesize -= bufSize;
                if (filesize == 0L) break;
            }

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
        }

        stream.getStreamCompleted().set(true);
        channel.disconnect();
        session.disconnect();
    }

    public int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //         -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    private static Session createSession(String user, String host, int port, String keyFilePath, String keyPassword) {
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
