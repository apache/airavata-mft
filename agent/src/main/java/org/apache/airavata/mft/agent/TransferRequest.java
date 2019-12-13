package org.apache.airavata.mft.agent;

import java.util.List;

public class TransferRequest {

    private String transferId;
    private String sourceId;
    private String sourceType;
    private String sourceToken;
    private String destinationId;
    private String destinationType;
    private String destinationToken;
    private List<String> agentList;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public String getDestinationToken() {
        return destinationToken;
    }

    public void setDestinationToken(String destinationToken) {
        this.destinationToken = destinationToken;
    }

    public List<String> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<String> agentList) {
        this.agentList = agentList;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }
}
