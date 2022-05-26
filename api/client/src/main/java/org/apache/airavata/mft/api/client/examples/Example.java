package org.apache.airavata.mft.api.client.examples;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.FetchResourceMetadataRequest;
import org.apache.airavata.mft.api.service.MFTApiServiceGrpc;
import org.apache.airavata.mft.api.service.ResourceAvailabilityRequest;

public class Example {

    public static void main(String a[]) {
        MFTApiServiceGrpc.MFTApiServiceBlockingStub mftClient = new MFTApiClient("localhost", 7004).get();
        mftClient.getResourceAvailability(ResourceAvailabilityRequest.newBuilder()
                .setResourceId("a")
                .setResourceToken("b")
                .setResourceType("SCP")
                .setResourceBackend("AIRAVATA")
                .setResourceCredentialBackend("AIRAVATA").build());
        System.out.println("Hooooo");
    }
}
