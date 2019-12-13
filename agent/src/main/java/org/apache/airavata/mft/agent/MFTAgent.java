package org.apache.airavata.mft.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.KVCache;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.transport.local.LocalMetadataCollector;
import org.apache.airavata.mft.transport.local.LocalReceiver;
import org.apache.airavata.mft.transport.local.LocalSender;
import org.apache.airavata.mft.transport.scp.SCPMetadataCollector;
import org.apache.airavata.mft.transport.scp.SCPReceiver;
import org.apache.airavata.mft.transport.scp.SCPSender;

import java.util.Optional;
import java.util.concurrent.Semaphore;

public class MFTAgent {

    private TransportMediator mediator = new TransportMediator();
    private String agentId = "agent0";
    private Semaphore mainHold = new Semaphore(0);

    private void acceptRequests() {
        Consul client = Consul.builder().build();
        KeyValueClient kvClient = client.keyValueClient();

        KVCache messageCache = KVCache.newCache(kvClient, agentId + "/messages");
        messageCache.addListener(newValues -> {
            // Cache notifies all paths with "foo" the root path
            // If you want to watch only "foo" value, you must filter other paths

            newValues.values().forEach(value -> {

                // Values are encoded in key/value store, decode it if needed
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    System.out.println(String.format("Value is: %s", v));
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        TransferRequest request = mapper.readValue(v, TransferRequest.class);
                        System.out.println("Received request " + request.getTransferId());

                        Connector inConnector = resolveConnector(request.getSourceType(), "IN");
                        inConnector.init(request.getSourceId(), request.getSourceToken());
                        Connector outConnector = resolveConnector(request.getDestinationType(), "OUT");
                        outConnector.init(request.getDestinationId(), request.getDestinationToken());

                        MetadataCollector metadataCollector = resolveMetadataCollector(request.getSourceType());
                        ResourceMetadata metadata = metadataCollector.getGetResourceMetadata(request.getSourceId(), request.getSourceToken());
                        System.out.println("File size " + metadata.getResourceSize());
                        String transferId = mediator.transfer(inConnector, outConnector, metadata);
                        System.out.println("Submitted transfer " + transferId);

                        System.out.println("Deleting key " + value.getKey());
                        kvClient.deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }); //prints "bar"

            });
        });
        messageCache.start();
    }


    public static void main(String args[]) throws InterruptedException {
        MFTAgent agent = new MFTAgent();
        agent.acceptRequests();
        agent.mainHold.acquire();
    }

    // TODO load from reflection to avoid dependencies
    private Connector resolveConnector(String type, String direction) {
        switch (type) {
            case "SCP":
                switch (direction) {
                    case "IN":
                        return new SCPReceiver();
                    case "OUT":
                        return new SCPSender();
                }
                break;
            case "LOCAL":
                switch (direction) {
                    case "IN":
                        return new LocalReceiver();
                    case "OUT":
                        return new LocalSender();
                }
        }
        return null;
    }

    // TODO load from reflection to avoid dependencies
    private MetadataCollector resolveMetadataCollector(String type) {
        switch (type) {
            case "SCP":
                return new SCPMetadataCollector();
            case "LOCAL":
                return new LocalMetadataCollector();
        }
        return null;
    }
}
