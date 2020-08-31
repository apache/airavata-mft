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

package org.apache.airavata.mft.controller.sql.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TransferRequestEntity {

    @Id
    @Column(name = "TRANSFER_ID")
    private String transferId;

    @Column(name = "SOURCE_ID")
    private String sourceId;

    @Column(name = "SOURCE_TYPE")
    private String sourceType;

    @Column(name = "SOURCE_TOKEN")
    private String sourceToken;

    @Column(name = "SOURCE_RESOURCE_BACKEND")
    private String sourceResourceBackend;

    @Column(name = "SOURCE_CREDENTIAL_BACKEND")
    private String sourceCredentialBackend;

    @Column(name = "DESTINATION_ID")
    private String destinationId;

    @Column(name = "DESTINATION_TYPE")
    private String destinationType;

    @Column(name = "DESTINATION_TOKEN")
    private String destinationToken;

    @Column(name = "DESTINATION_RESOURCE_BACKEND")
    private String destResourceBackend;

    @Column(name = "DESTINATION_CREDENTIAL_BACKEND")
    private String destCredentialBackend;

    @Column(name = "TRANSFER_STATUS")
    private String status;

    @Column(name = "INITIAL_EPOCH_TIME")
    private Long initialEpochTimeInMillis;

    @Column(name = "CURRENT_EPOCH_TIME")
    private Long currentEpochTimeInMillis;

    public String getTransferId() {
        return transferId;
    }

    public TransferRequestEntity setTransferId(String transferId) {
        this.transferId = transferId;
        return this;
    }

    public String getSourceId() {
        return sourceId;
    }

    public TransferRequestEntity setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public TransferRequestEntity setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public TransferRequestEntity setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
        return this;
    }

    public String getSourceResourceBackend() {
        return sourceResourceBackend;
    }

    public TransferRequestEntity setSourceResourceBackend(String sourceResourceBackend) {
        this.sourceResourceBackend = sourceResourceBackend;
        return this;
    }

    public String getSourceCredentialBackend() {
        return sourceCredentialBackend;
    }

    public TransferRequestEntity setSourceCredentialBackend(String sourceCredentialBackend) {
        this.sourceCredentialBackend = sourceCredentialBackend;
        return this;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public TransferRequestEntity setDestinationId(String destinationId) {
        this.destinationId = destinationId;
        return this;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public TransferRequestEntity setDestinationType(String destinationType) {
        this.destinationType = destinationType;
        return this;
    }

    public String getDestinationToken() {
        return destinationToken;
    }

    public TransferRequestEntity setDestinationToken(String destinationToken) {
        this.destinationToken = destinationToken;
        return this;
    }

    public String getDestResourceBackend() {
        return destResourceBackend;
    }

    public TransferRequestEntity setDestResourceBackend(String destResourceBackend) {
        this.destResourceBackend = destResourceBackend;
        return this;
    }

    public String getDestCredentialBackend() {
        return destCredentialBackend;
    }

    public TransferRequestEntity setDestCredentialBackend(String destCredentialBackend) {
        this.destCredentialBackend = destCredentialBackend;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TransferRequestEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public Long getInitialEpochTimeInMillis() {
        return initialEpochTimeInMillis;
    }

    public TransferRequestEntity setInitialEpochTimeInMillis(Long epochTimeInMillis) {
        this.initialEpochTimeInMillis = epochTimeInMillis;
        return this;
    }

    public Long getCurrentEpochTimeInMillis() {
        return currentEpochTimeInMillis;
    }

    public TransferRequestEntity setCurrentEpochTimeInMillis(Long epochTimeInMillis) {
        this.currentEpochTimeInMillis = epochTimeInMillis;
        return this;
    }
}
