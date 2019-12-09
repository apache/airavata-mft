package org.apache.airavata.mft.transport.scp;

import net.ladenthin.streambuffer.StreamBuffer;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.TransferTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TransportMediator {

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String args[]) throws Exception {
        TransportMediator mediator = new TransportMediator();
        SCPReceiver receiver = new SCPReceiver();
        receiver.init("1", "");

        SCPSender scpSender = new SCPSender();
        scpSender.init("2", "");

        SCPMetadataCollector metadataCollector = new SCPMetadataCollector();
        ResourceMetadata metadata = metadataCollector.getGetResourceMetadata("1", "");
        mediator.transfer(receiver, scpSender, metadata);
    }

    public void transfer(SCPReceiver receiver, SCPSender sender, ResourceMetadata metadata) throws Exception {

        StreamBuffer streamBuffer = new StreamBuffer();
        ConnectorContext context = new ConnectorContext();
        context.setMetadata(metadata);
        context.setStreamBuffer(streamBuffer);

        TransferTask recvTask = new TransferTask(receiver, context);
        TransferTask sendTask = new TransferTask(sender, context);

        Future<Integer> recvFuture = executor.submit(recvTask);
        Future<Integer> sendFuture = executor.submit(sendTask);

        executor.shutdown();
    }
}
