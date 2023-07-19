package org.apache.airavata.mft.secret.server.backend.sql.entity.http;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class TokenAuthSecretEntity {

    @Id
    @Column(name = "SECRET_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String secretId;


    @Lob
    @Column(name = "ACCESS_TOKEN", columnDefinition = "TEXT")
    String accessToken;

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
