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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class GCSStorageEntity
{

    @Id
    @Column(name = "GCS_STORAGE_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator( name = "uuid", strategy = "uuid2")
    private String storageId;

    @Column(name = "BUCKET_NAME")
    private String bucketName;

    @Column(name = "STORAGE_NAME")
    private String name;

    public String getStorageId()
    {
        return storageId;
    }

    public void setStorageId( String storageId )
    {
        this.storageId = storageId;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName( String bucketName )
    {
        this.bucketName = bucketName;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }
}
