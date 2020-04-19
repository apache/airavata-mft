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
    public String transfer() {

        MFTApiServiceGrpc.MFTApiServiceBlockingStub client = MFTApiClient.buildClient("localhost", 7004);

        String sourceId = "gcs-bucket";
        String sourceToken ="gcs-cred" ;
        String destId = "s3-file";
        String destToken ="s3-cred";

        TransferApiRequest request = TransferApiRequest.newBuilder()
                .setSourceId(sourceId)
                .setSourceToken(sourceToken)
                .setSourceType("GCS")
                .setDestinationId(destId)
                .setDestinationToken(destToken)
                .setDestinationType("S3")
                .setAffinityTransfer(false).build();

        // Submitting the transfer to MFT
        TransferApiResponse transferApiResponse = client.submitTransfer(request);
        return transferApiResponse.getTransferId();
//        while(true) {
//            // Monitoring transfer status
//            try {
//                TransferStateApiResponse transferState = client.getTransferState(TransferStateApiRequest.newBuilder().setTransferId(transferApiResponse.getTransferId()).build());
//                System.out.println("Latest Transfer State " + transferState.getState());
//
//            } catch (Exception e) {
//                System.out.println("Errored " + e.getMessage());
//                return "Error!";
//            }
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                return "Greetings from Spring Boot!";
//            }
//        }
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