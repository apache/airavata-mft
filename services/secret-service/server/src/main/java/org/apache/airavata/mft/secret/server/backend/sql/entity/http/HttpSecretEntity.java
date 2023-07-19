package org.apache.airavata.mft.secret.server.backend.sql.entity.http;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class HttpSecretEntity {
    @Id
    @Column(name = "SECRET_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String secretId;

    public enum InternalSecretType {
        BASIC, TOKEN;
    }

    @Column(name = "INTERNAL_SECRET_ID")
    private String internalSecretId;

    @Column(name = "INTERNAL_SECRET_TYPE")
    private HttpSecretEntity.InternalSecretType internalSecretType;

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getInternalSecretId() {
        return internalSecretId;
    }

    public void setInternalSecretId(String internalSecretId) {
        this.internalSecretId = internalSecretId;
    }

    public InternalSecretType getInternalSecretType() {
        return internalSecretType;
    }

    public void setInternalSecretType(InternalSecretType internalSecretType) {
        this.internalSecretType = internalSecretType;
    }
}
