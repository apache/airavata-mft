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

package org.apache.airavata.mft.controller.db.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
public class TransferEntity {
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

    @Column(name = "DEST_RESOURCE_BACKEND")
    private String destResourceBackend;

    @Column(name = "DEST_CREDENTIAL_BACKEND")
    private String destCredentialBackend;

    @Column(name = "AFFINITY_TRANSFER")
    private boolean affinityTransfer;

    @OneToMany(targetEntity = TargetAgentEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<TargetAgentEntity> targetAgents;

    @OneToMany(targetEntity = TransferExecutionEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<TransferExecutionEntity> executions;

    @OneToMany(targetEntity = TransferStatusEntity.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<TransferStatusEntity> statuses;

    public String getTransferId() {
        return transferId;
    }

    public TransferEntity setTransferId(String transferId) {
        this.transferId = transferId;
        return this;
    }

    public String getSourceId() {
        return sourceId;
    }

    public TransferEntity setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public TransferEntity setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public TransferEntity setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
        return this;
    }

    public String getSourceResourceBackend() {
        return sourceResourceBackend;
    }

    public TransferEntity setSourceResourceBackend(String sourceResourceBackend) {
        this.sourceResourceBackend = sourceResourceBackend;
        return this;
    }

    public String getSourceCredentialBackend() {
        return sourceCredentialBackend;
    }

    public TransferEntity setSourceCredentialBackend(String sourceCredentialBackend) {
        this.sourceCredentialBackend = sourceCredentialBackend;
        return this;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public TransferEntity setDestinationId(String destinationId) {
        this.destinationId = destinationId;
        return this;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public TransferEntity setDestinationType(String destinationType) {
        this.destinationType = destinationType;
        return this;
    }

    public String getDestinationToken() {
        return destinationToken;
    }

    public TransferEntity setDestinationToken(String destinationToken) {
        this.destinationToken = destinationToken;
        return this;
    }

    public String getDestResourceBackend() {
        return destResourceBackend;
    }

    public TransferEntity setDestResourceBackend(String destResourceBackend) {
        this.destResourceBackend = destResourceBackend;
        return this;
    }

    public String getDestCredentialBackend() {
        return destCredentialBackend;
    }

    public TransferEntity setDestCredentialBackend(String destCredentialBackend) {
        this.destCredentialBackend = destCredentialBackend;
        return this;
    }

    public boolean isAffinityTransfer() {
        return affinityTransfer;
    }

    public TransferEntity setAffinityTransfer(boolean affinityTransfer) {
        this.affinityTransfer = affinityTransfer;
        return this;
    }

    public Set<TargetAgentEntity> getTargetAgents() {
        return targetAgents;
    }

    public TransferEntity setTargetAgents(Set<TargetAgentEntity> targetAgents) {
        this.targetAgents = targetAgents;
        return this;
    }

    public Set<TransferExecutionEntity> getExecutions() {
        return executions;
    }

    public TransferEntity setExecutions(Set<TransferExecutionEntity> executions) {
        this.executions = executions;
        return this;
    }

    public Set<TransferStatusEntity> getStatuses() {
        return statuses;
    }

    public TransferEntity setStatuses(Set<TransferStatusEntity> statuses) {
        this.statuses = statuses;
        return this;
    }
}
