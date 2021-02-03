package org.apache.airavata.mft.secret.server.backend.custos.auth;

/**
 * Represents the Auth object
 */
public class AuthConfig {

    private String id;
    private String secret;
    private String accessToken;
    private String refreshToken;
    private String idToken;
    private String custosId;

    public AuthConfig(String id, String secret, String accessToken, String refreshToken, String idToken) {
        this.id = id;
        this.secret = secret;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.idToken = idToken;
    }

    public AuthConfig() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getCustosId() {
        return custosId;
    }

    public void setCustosId(String custosId) {
        this.custosId = custosId;
    }
}
