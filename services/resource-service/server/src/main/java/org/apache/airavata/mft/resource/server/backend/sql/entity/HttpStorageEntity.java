package org.apache.airavata.mft.resource.server.backend.sql.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class HttpStorageEntity {

    @Id
    @Column(name = "HTTP_STORAGE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator( name = "uuid", strategy = "uuid2")
    private String storageId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "BASE_URL")
    private String baseUrl;

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
