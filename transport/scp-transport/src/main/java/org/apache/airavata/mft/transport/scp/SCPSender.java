package org.apache.airavata.mft.transport.scp;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import net.ladenthin.streambuffer.StreamBuffer;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SCPSender implements Connector {
    private SSHResourceIdentifier sshResourceIdentifier;
    private Session session;
    public void init(String resourceId, String credentialToken) {
        this.sshResourceIdentifier = SCPTransportUtil.getSSHResourceIdentifier(resourceId);
        this.session = SCPTransportUtil.createSession(sshResourceIdentifier.getUser(), sshResourceIdentifier.getHost(),
                sshResourceIdentifier.getPort(),
                sshResourceIdentifier.getKeyFile(),
                sshResourceIdentifier.getKeyPassphrase());
    }

    public void destroy() {
        this.session.disconnect();
    }

    public void startStream(ConnectorContext context) throws Exception {
        copyLocalToRemote(this.session, sshResourceIdentifier.getRemotePath(), context.getStreamBuffer(), context.getMetadata().getResourceSize());
    }

    private void copyLocalToRemote(Session session, String to, StreamBuffer streamBuffer, long fileSize) throws JSchException, IOException {

        InputStream inputStream = streamBuffer.getInputStream();

        boolean ptimestamp = true;

        // exec 'scp -t rfile' remotely
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + to;
        com.jcraft.jsch.Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        if (checkAck(in) != 0) {
            System.exit(0);
        }

        if (ptimestamp) {
            command = "T" + (System.currentTimeMillis() / 1000) + " 0";
            // The access time should be sent here,
            // but it is not accessible with JavaAPI ;-<
            command += (" " + (System.currentTimeMillis() / 1000) + " 0\n");
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                System.exit(0);
            }
        }

        // send "C0644 filesize filename", where filename should not include '/'
        command = "C0644 " + fileSize + " ";
        if (to.lastIndexOf('/') > 0) {
            command += to.substring(to.lastIndexOf('/') + 1);
        } else {
            command += to;
        }

        command += "\n";
        out.write(command.getBytes());
        out.flush();

        if (checkAck(in) != 0) {
            System.exit(0);
        }

        // send a content of lfile
        byte[] buf = new byte[1024];
        long totalWritten = 0;
        while (true) {
            int len = inputStream.read(buf, 0, buf.length);
            if (len == -1) {
                break;
            } else {
                out.write(buf, 0, len); //out.flush();
                totalWritten += len;
                if (totalWritten == fileSize) {
                    break;
                }
            }
        }

        // send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        if (checkAck(in) != 0) {
            System.exit(0);
        }
        out.close();

        channel.disconnect();
        session.disconnect();
        System.out.println("Done sending");
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
}
