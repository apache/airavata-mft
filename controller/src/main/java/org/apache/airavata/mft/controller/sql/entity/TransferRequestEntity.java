package org.apache.airavata.mft.controller.sql.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TransferRequestEntity {

    @Id
    private String transferId;
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
    private String status;

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
}
