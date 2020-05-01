/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
