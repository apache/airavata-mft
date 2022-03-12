package org.apache.airavata.mft.resource.server.backend.sql.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class GenericResourceEntity {

    public enum ResourceType {
        FILE, DIRECTORY;
    }

    public enum StorageType {
        S3, SCP, LOCAL, FTP, BOX, DROPBOX, GCS, AZURE;
    }

    @Id
    @Column(name = "RESOURCE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String resourceId;

    @Column(name = "RESOURCE_PATH")
    private String resourcePath;

    @Column(name = "RESOURCE_TYPE")
    private ResourceType resourceType;

    @Column(name = "STORAGE_ID")
    private String storageId;

    @Column(name = "STORAGE_TYPE")
    private StorageType storageType;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }
}
