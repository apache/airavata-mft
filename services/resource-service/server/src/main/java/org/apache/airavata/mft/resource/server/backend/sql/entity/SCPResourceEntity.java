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
public class SCPResourceEntity {

    @Id
    @Column(name = "SCP_RESOURCE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String resourceId;

    @Column(name = "SCP_STORAGE_ID")
    private String scpStorageId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SCP_STORAGE_ID", referencedColumnName = "SCP_STORAGE_ID", nullable = false, insertable = false, updatable = false)
    private SCPStorageEntity scpStorage;

    @Column(name = "RESOURCE_PATH")
    private String resourcePath;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public SCPStorageEntity getScpStorage() {
        return scpStorage;
    }

    public void setScpStorage(SCPStorageEntity scpStorage) {
        this.scpStorage = scpStorage;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getScpStorageId() {
        return scpStorageId;
    }

    public void setScpStorageId(String scpStorageId) {
        this.scpStorageId = scpStorageId;
    }
}
