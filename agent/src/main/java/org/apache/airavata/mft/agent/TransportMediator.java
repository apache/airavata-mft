package org.apache.airavata.mft.agent;

import net.ladenthin.streambuffer.StreamBuffer;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.TransferTask;
import org.apache.airavata.mft.core.api.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TransportMediator {

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public void destroy() {
        executor.shutdown();
    }

    public void transfer(Connector inConnector, Connector outConnector, ResourceMetadata metadata) throws Exception {

        StreamBuffer streamBuffer = new StreamBuffer();
        ConnectorContext context = new ConnectorContext();
        context.setMetadata(metadata);
        context.setStreamBuffer(streamBuffer);

        TransferTask recvTask = new TransferTask(inConnector, context);
        TransferTask sendTask = new TransferTask(outConnector, context);
        List<Future<Integer>> futureList = new ArrayList<>();

        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        futureList.add(completionService.submit(recvTask));
        futureList.add(completionService.submit(sendTask));

        for (int i = 0; i < futureList.size(); i++) {
            Future<Integer> ft = completionService.take();
            futureList.remove(ft);
            try {
                ft.get();
            } catch(InterruptedException e){
                // Interrupted
            } catch(ExecutionException e){
                // Snap, something went wrong in the task! Abort! Abort! Abort!
                System.out.println("One task failed with error: " + e.getMessage() );
                e.printStackTrace();
                for(Future<Integer> f : futureList){
                    f.cancel(true);
                }
                futureList.clear();
            }
        }

        //inConnector.destroy();
        //outConnector.destroy();
    }
}
