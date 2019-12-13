/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.agent;

import org.apache.airavata.mft.core.CircularStreamingBuffer;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.TransferTask;
import org.apache.airavata.mft.core.api.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class TransportMediator {

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private ExecutorService monitor = Executors.newFixedThreadPool(10);

    public void destroy() {
        executor.shutdown();
    }

    public String transfer(Connector inConnector, Connector outConnector, ResourceMetadata metadata) throws Exception {

        String transferId = UUID.randomUUID().toString();

        CircularStreamingBuffer streamBuffer = new CircularStreamingBuffer();
        ConnectorContext context = new ConnectorContext();
        context.setMetadata(metadata);
        context.setStreamBuffer(streamBuffer);

        TransferTask recvTask = new TransferTask(inConnector, context);
        TransferTask sendTask = new TransferTask(outConnector, context);
        List<Future<Integer>> futureList = new ArrayList<>();

        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        long startTime = System.currentTimeMillis();

        futureList.add(completionService.submit(recvTask));
        futureList.add(completionService.submit(sendTask));

        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    for (int i = 0; i < futureList.size(); i++) {
                        Future<Integer> ft = completionService.take();
                        futureList.remove(ft);
                        try {
                            ft.get();
                        } catch (InterruptedException e) {
                            // Interrupted
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            // Snap, something went wrong in the task! Abort! Abort! Abort!
                            System.out.println("One task failed with error: " + e.getMessage());
                            e.printStackTrace();
                            for (Future<Integer> f : futureList) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                                f.cancel(true);
                            }
                            futureList.clear();
                        }
                    }

                    long endTime = System.currentTimeMillis();

                    long time = (endTime - startTime) / 1000;

                    System.out.println("Transfer Speed " + (metadata.getResourceSize() * 1.0 / time) / (1024 * 1024) + " MB/s");
                    System.out.println("Transfer " + transferId + " completed");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    inConnector.destroy();
                    outConnector.destroy();
                }
            }
        });

        monitor.submit(monitorThread);
        return transferId;
    }
}
