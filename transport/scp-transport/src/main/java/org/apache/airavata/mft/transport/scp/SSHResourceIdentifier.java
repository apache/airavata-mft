package org.apache.airavata.mft.transport.scp;

public class SSHResourceIdentifier {
    private String id;
    private String remotePath;
    private String host;
    private String user;
    private int port;
    private String keyFile;
    private String keyPassphrase;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getKeyPassphrase() {
        return keyPassphrase;
    }

    public void setKeyPassphrase(String keyPassphrase) {
        this.keyPassphrase = keyPassphrase;
    }
}
