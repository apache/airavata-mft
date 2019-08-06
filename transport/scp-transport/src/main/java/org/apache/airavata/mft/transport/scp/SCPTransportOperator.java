package org.apache.airavata.mft.transport.scp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import org.apache.airavata.mft.core.api.TransportOperator;

public class SCPTransportOperator implements TransportOperator {
    @Override
    public long getResourceSize(String resourceId) throws Exception {
        SSHResourceIdentifier sshResourceIdentifier = SCPTransportUtil.getSSHResourceIdentifier(resourceId);
        Session session = SCPTransportUtil.createSession(sshResourceIdentifier.getUser(), sshResourceIdentifier.getHost(),
                sshResourceIdentifier.getPort(),
                sshResourceIdentifier.getKeyFile(),
                sshResourceIdentifier.getKeyPassphrase());
        Channel channel=session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftp =(ChannelSftp)channel;
        SftpATTRS stat = sftp.stat(sshResourceIdentifier.getRemotePath());
        channel.disconnect();
        session.disconnect();
        return stat.getSize();
    }
}
