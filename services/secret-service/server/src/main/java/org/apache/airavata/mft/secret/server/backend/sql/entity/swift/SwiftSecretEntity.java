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
public class SwiftSecretEntity {

    public enum InternalSecretType {
        V2, V3;
    }

    @Id
    @Column(name = "SECRET_ID")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String secretId;

    @Column(name = "INTERNAL_SECRET_ID")
    private String internalSecretId;

    @Column(name = "INTERNAL_SECRET_TYPE")
    private InternalSecretType internalSecretType;

    @Column(name = "ENDPOINT")
    private String endpoint;

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

    public String getEndpoint() {
        return endpoint;
    }

    public SwiftSecretEntity setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }
}
