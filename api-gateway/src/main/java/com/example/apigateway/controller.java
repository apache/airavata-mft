package com.example.apigateway;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.*;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class controller {
    @RequestMapping(value = "/transfer", method = POST)
    public String transfer(@RequestBody transferRequest transferRequest) {

        MFTApiServiceGrpc.MFTApiServiceBlockingStub client = MFTApiClient.buildClient("localhost", 7004);

        String sourceId =transferRequest.getSourceId();
        String sourceToken =transferRequest.getSourceToken();
        String destId = transferRequest.getDestId();
        String destToken =transferRequest.getDestToken() ;
        String sourceType=transferRequest.getSourceType();
        String destinationType=transferRequest.getDestinationType();
        TransferApiRequest request = TransferApiRequest.newBuilder()
                .setSourceId(sourceId)
                .setSourceToken(sourceToken)
                .setSourceType(sourceType)
                .setDestinationId(destId)
                .setDestinationToken(destToken)
                .setDestinationType(destinationType)
                .setAffinityTransfer(false).build();

        // Submitting the transfer to MFT
        TransferApiResponse transferApiResponse = client.submitTransfer(request);
        return transferApiResponse.getTransferId();
    }

    @RequestMapping(value = "/status",method = POST,consumes="application/json")
    public String status(@RequestBody transferId transferId) {
        MFTApiServiceGrpc.MFTApiServiceBlockingStub client = MFTApiClient.buildClient("localhost", 7004);
        // Monitoring transfer status
        try {
            TransferStateApiResponse transferState = client.getTransferState(TransferStateApiRequest.newBuilder().setTransferId(transferId.transferId).build());
            System.out.println("Latest Transfer State " + transferState.getState());
            return "Latest Transfer State " + transferState.getState();

        } catch (Exception e) {
            System.out.println("Errored " + e.getMessage());
            return "Errored " + e.getMessage();
        }
    }
}

class transferId
{
    String transferId;
    public String getTransferId() {
        return transferId;
    }
    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }
}

class transferRequest{

    String sourceId ;
    String sourceToken ;
    String destId ;
    String destToken  ;
    String sourceType;
    String destinationType;

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

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceToken() {
        return sourceToken;
    }

    public void setSourceToken(String sourceToken) {
        this.sourceToken = sourceToken;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public String getDestToken() {
        return destToken;
    }

    public void setDestToken(String destToken) {
        this.destToken = destToken;
    }

}