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

package org.apache.airavata.mft.admin.models;

import java.util.Map;

public class TransferRequest {

    private String sourceId;
    private String sourceType;
    private String sourceToken;
    private String sourceResourceBackend;
    private String sourceCredentialBackend;
    private String destinationId;
    private String destinationType;
    private String destinationToken;
    private String destResourceBackend;
    private String destCredentialBackend;
    private String mftAuthorizationToken;
    private boolean affinityTransfer;
    private Map<String, Integer> targetAgents;


    public String getSourceId() {
        return sourceId;
    }

    public TransferRequest setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public TransferRequest setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public TransferRequest setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
        return this;
    }

    public String getSourceResourceBackend() {
        return sourceResourceBackend;
    }

    public TransferRequest setSourceResourceBackend(String sourceResourceBackend) {
        this.sourceResourceBackend = sourceResourceBackend;
        return this;
    }

    public String getSourceCredentialBackend() {
        return sourceCredentialBackend;
    }

    public TransferRequest setSourceCredentialBackend(String sourceCredentialBackend) {
        this.sourceCredentialBackend = sourceCredentialBackend;
        return this;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public TransferRequest setDestinationId(String destinationId) {
        this.destinationId = destinationId;
        return this;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public TransferRequest setDestinationType(String destinationType) {
        this.destinationType = destinationType;
        return this;
    }

    public String getDestinationToken() {
        return destinationToken;
    }

    public TransferRequest setDestinationToken(String destinationToken) {
        this.destinationToken = destinationToken;
        return this;
    }

    public String getDestResourceBackend() {
        return destResourceBackend;
    }

    public TransferRequest setDestResourceBackend(String destResourceBackend) {
        this.destResourceBackend = destResourceBackend;
        return this;
    }

    public String getDestCredentialBackend() {
        return destCredentialBackend;
    }

    public TransferRequest setDestCredentialBackend(String destCredentialBackend) {
        this.destCredentialBackend = destCredentialBackend;
        return this;
    }

    public boolean isAffinityTransfer() {
        return affinityTransfer;
    }

    public TransferRequest setAffinityTransfer(boolean affinityTransfer) {
        this.affinityTransfer = affinityTransfer;
        return this;
    }

    public Map<String, Integer> getTargetAgents() {
        return targetAgents;
    }

    public TransferRequest setTargetAgents(Map<String, Integer> targetAgents) {
        this.targetAgents = targetAgents;
        return this;
    }

    public String getMftAuthorizationToken() {
        return mftAuthorizationToken;
    }

    public void setMftAuthorizationToken(String mftAuthorizationToken) {
        this.mftAuthorizationToken = mftAuthorizationToken;
    }
}
