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
