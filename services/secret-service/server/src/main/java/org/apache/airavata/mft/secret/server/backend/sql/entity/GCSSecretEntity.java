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

package org.apache.airavata.mft.secret.server.backend.sql.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

@Entity
public class GCSSecretEntity
{

    @Id
    @Column(name = "SECRET_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator( name = "uuid", strategy = "uuid2")
    private String secretId;

    @Column(name = "PROJECT_ID")
    private String projectId;

    @Column(name = "PRIVATE_KEY", length = 3000)
    private String privateKey;

    @Column(name = "CLIENT_EMAIL")
    private String clientEmail;


    public String getSecretId()
    {
        return secretId;
    }

    public void setSecretId( String secretId )
    {
        this.secretId = secretId;
    }

    public String getProjectId()
    {
        return projectId;
    }

    public void setProjectId( String projectId )
    {
        this.projectId = projectId;
    }

    public String getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey( String privateKey )
    {
        this.privateKey = privateKey;
    }

    public String getClientEmail()
    {
        return clientEmail;
    }

    public void setClientEmail( String clientEmail )
    {
        this.clientEmail = clientEmail;
    }
}
