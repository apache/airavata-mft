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

public class TransferCommand {

    private String transferId;
    private String sourceStorageId;
    private String sourcePath;
    private String sourceType;
    private String sourceToken;
    private String sourceResourceBackend;
    private String sourceCredentialBackend;
    private String destinationStorageId;
    private String destinationPath;
    private String destinationType;
    private String destinationToken;
    private String destResourceBackend;
    private String destCredentialBackend;
    private String mftAuthorizationToken;

    public String getTransferId() {
        return transferId;
    }

    public TransferCommand setTransferId(String transferId) {
        this.transferId = transferId;
        return this;
    }

    public String getSourceStorageId() {
        return sourceStorageId;
    }

    public TransferCommand setSourceStorageId(String sourceStorageId) {
        this.sourceStorageId = sourceStorageId;
        return this;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public TransferCommand setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public TransferCommand setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public TransferCommand setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
        return this;
    }

    public String getSourceResourceBackend() {
        return sourceResourceBackend;
    }

    public TransferCommand setSourceResourceBackend(String sourceResourceBackend) {
        this.sourceResourceBackend = sourceResourceBackend;
        return this;
    }

    public String getSourceCredentialBackend() {
        return sourceCredentialBackend;
    }

    public TransferCommand setSourceCredentialBackend(String sourceCredentialBackend) {
        this.sourceCredentialBackend = sourceCredentialBackend;
        return this;
    }

    public String getDestinationStorageId() {
        return destinationStorageId;
    }

    public TransferCommand setDestinationStorageId(String destinationStorageId) {
        this.destinationStorageId = destinationStorageId;
        return this;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public TransferCommand setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
        return this;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public TransferCommand setDestinationType(String destinationType) {
        this.destinationType = destinationType;
        return this;
    }

    public String getDestinationToken() {
        return destinationToken;
    }

    public TransferCommand setDestinationToken(String destinationToken) {
        this.destinationToken = destinationToken;
        return this;
    }

    public String getDestResourceBackend() {
        return destResourceBackend;
    }

    public TransferCommand setDestResourceBackend(String destResourceBackend) {
        this.destResourceBackend = destResourceBackend;
        return this;
    }

    public String getDestCredentialBackend() {
        return destCredentialBackend;
    }

    public TransferCommand setDestCredentialBackend(String destCredentialBackend) {
        this.destCredentialBackend = destCredentialBackend;
        return this;
    }

    public String getMftAuthorizationToken() {
        return mftAuthorizationToken;
    }

    public void setMftAuthorizationToken(String mftAuthorizationToken) {
        this.mftAuthorizationToken = mftAuthorizationToken;
    }
}
