package org.apache.airavata.mft.agent;

import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.transport.local.LocalMetadataCollector;
import org.apache.airavata.mft.transport.local.LocalReceiver;
import org.apache.airavata.mft.transport.local.LocalSender;
import org.apache.airavata.mft.transport.scp.SCPMetadataCollector;
import org.apache.airavata.mft.transport.scp.SCPReceiver;
import org.apache.airavata.mft.transport.scp.SCPSender;

import java.util.ArrayList;
import java.util.List;

public class MFTAgent {

    private List<TransferRequest> requests = new ArrayList<>();
    private TransportMediator mediator = new TransportMediator();

    public void acceptRequests() {
        for (TransferRequest request : requests) {

            try {
                Connector inConnector = resolveConnector(request.getSourceType(), "IN");
                inConnector.init(request.getSourceId(), request.getSourceToken());
                Connector outConnector = resolveConnector(request.getDestinationType(), "OUT");
                outConnector.init(request.getDestinationId(), request.getDestinationToken());

                MetadataCollector metadataCollector = resolveMetadataCollector(request.getSourceType());
                ResourceMetadata metadata = metadataCollector.getGetResourceMetadata(request.getSourceId(), request.getSourceToken());
                System.out.println("File size " + metadata.getResourceSize());
                mediator.transfer(inConnector, outConnector, metadata);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String args[]) {
        TransferRequest request = new TransferRequest();
        request.setSourceId("1");
        request.setSourceType("SCP");
        request.setDestinationId("2");
        request.setDestinationType("LOCAL");

        MFTAgent agent = new MFTAgent();
        agent.requests.add(request);
        agent.acceptRequests();
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
