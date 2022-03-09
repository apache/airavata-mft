package org.apache.airavata.mft.api.client.examples;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.MFTTransferServiceGrpc;
import org.apache.airavata.mft.api.service.ResourceAvailabilityRequest;

public class Example {

    public static void main(String a[]) {
        MFTTransferServiceGrpc.MFTTransferServiceBlockingStub mftClient =  MFTApiClient.MFTApiClientBuilder
                .newBuilder().build().getTransferClient();
        mftClient.getResourceAvailability(ResourceAvailabilityRequest.newBuilder()
                .setResourceId("a")
                .setResourceToken("b")
                .setResourceType("SCP")
                .setResourceBackend("AIRAVATA")
                .setResourceCredentialBackend("AIRAVATA").build());
        System.out.println("Hooooo");
    }
}
