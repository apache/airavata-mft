/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.mft.transport.scp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.airavata.mft.core.api.StreamedSender;
import org.apache.airavata.mft.core.streaming.TransportStream;

import java.io.*;

public class SCPSender implements StreamedSender {

    @Override
    public void send(TransportStream stream) throws Exception {
        SSHResourceIdentifier sshResourceIdentifier = SCPTransportUtil.getSSHResourceIdentifier(stream.getDestId());
        Session session = SCPTransportUtil.createSession(sshResourceIdentifier.getUser(), sshResourceIdentifier.getHost(),
                sshResourceIdentifier.getPort(),
                sshResourceIdentifier.getKeyFile(),
                sshResourceIdentifier.getKeyPassphrase());
        copyLocalToRemote(session, sshResourceIdentifier.getRemotePath(), stream);
    }

    private static void copyLocalToRemote(Session session, String to, TransportStream stream) throws JSchException, IOException {
        boolean ptimestamp = true;

        // exec 'scp -t rfile' remotely
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + to;
        Channel channel = session.openChannel("exec");
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
        long filesize = stream.getMetadata().getLength();
        command = "C0644 " + filesize + " ";
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
            int len = stream.getInputStream().read(buf, 0, buf.length);
            if (len <= 0) {
                if (totalWritten == stream.getMetadata().getLength()) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //ignore
                }
                System.out.println("SCP Sender waiting for data. Wrote " + totalWritten + " Total " + stream.getMetadata().getLength());
            } else {
                out.write(buf, 0, len); //out.flush();
                totalWritten += len;
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

        try {
            if (stream.getInputStream() != null) stream.getInputStream().close();
        } catch (Exception ex) {
            System.out.println(ex);
        }

        channel.disconnect();
        session.disconnect();
    }

    public static int checkAck(InputStream in) throws IOException {
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
