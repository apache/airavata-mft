package org.apache.airavata.mft.core;

public class AuthZToken {

    private String mftAuthorizationToken;
    private String agentId;
    private String agentSecret;

    public AuthZToken(String mftAuthorizationToken, String agentId, String agentSecret) {
        this.mftAuthorizationToken = mftAuthorizationToken;
        this.agentId = agentId;
        this.agentSecret = agentSecret;
    }

    public AuthZToken(String mftAuthorizationToken) {
        this.mftAuthorizationToken = mftAuthorizationToken;
    }

    public String getMftAuthorizationToken() {
        return mftAuthorizationToken;
    }

    public void setMftAuthorizationToken(String mftAuthorizationToken) {
        this.mftAuthorizationToken = mftAuthorizationToken;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getAgentSecret() {
        return agentSecret;
    }

    public void setAgentSecret(String agentSecret) {
        this.agentSecret = agentSecret;
    }
}
