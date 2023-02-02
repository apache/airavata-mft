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

package org.apache.airavata.mft.secret.server.backend.sql.entity.swift;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class SwiftV3AuthSecretEntity {

    @Id
    @Column(name = "SECRET_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String secretId;

    @Column(name = "USER_DOMAIN_NAME")
    private String userDomainName;
    @Column(name = "USER_NAME")
    private String userName;
    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "TENANT_NAME")
    private String tenantName;

    @Column(name = "PROJECT_DOMAIN_NAME")
    private String projectDomainName;

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserDomainName() {
        return userDomainName;
    }

    public SwiftV3AuthSecretEntity setUserDomainName(String userDomainName) {
        this.userDomainName = userDomainName;
        return this;
    }

    public String getTenantName() {
        return tenantName;
    }

    public SwiftV3AuthSecretEntity setTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    public String getProjectDomainName() {
        return projectDomainName;
    }

    public SwiftV3AuthSecretEntity setProjectDomainName(String projectDomainName) {
        this.projectDomainName = projectDomainName;
        return this;
    }
}
