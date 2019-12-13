package org.apache.airavata.mft.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import java.util.Collections;

public class ConsulTester {
    public static void main(String args[]) throws Exception {
        Consul client = Consul.builder().build();
        KeyValueClient kvClient = client.keyValueClient();
        ObjectMapper mapper = new ObjectMapper();

        TransferRequest request = new TransferRequest();
        request.setSourceId("1");
        request.setSourceType("SCP");
        request.setDestinationId("2");
        request.setDestinationType("SCP");
        request.setAgentList(Collections.singletonList("agent0"));
        request.setTransferId("transfer005");

        String asString = mapper.writeValueAsString(request);

        kvClient.putValue("agent0/messages/" + request.getTransferId(), asString);
    }
}
