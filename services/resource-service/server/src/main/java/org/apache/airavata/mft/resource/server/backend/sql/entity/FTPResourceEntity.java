package org.apache.airavata.mft.resource.server.backend.sql.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class FTPResourceEntity {

    @Id
    @Column(name = "FTP_RESOURCE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String resourceId;

    @Column(name = "FTP_STORAGE_ID")
    private String ftpStorageId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FTP_STORAGE_ID", referencedColumnName = "FTP_STORAGE_ID", nullable = false, insertable = false, updatable = false)
    private FTPStorageEntity ftpStorage;

    @Column(name = "RESOURCE_PATH")
    private String resourcePath;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getFtpStorageId() {
        return ftpStorageId;
    }

    public void setFtpStorageId(String ftpStorageId) {
        this.ftpStorageId = ftpStorageId;
    }

    public FTPStorageEntity getFtpStorage() {
        return ftpStorage;
    }

    public void setFtpStorage(FTPStorageEntity ftpStorage) {
        this.ftpStorage = ftpStorage;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
}
