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

import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.core.*;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TransportMediator {

    private static final Logger logger = LoggerFactory.getLogger(TransportMediator.class);

    private ExecutorService executor = Executors.newFixedThreadPool(10);
    private ExecutorService monitor = Executors.newFixedThreadPool(10);

    public void destroy() {
        executor.shutdown();
    }

    public String transfer(String transferId, Connector inConnector, Connector outConnector, ResourceMetadata metadata,
                           BiConsumer<String, TransferState> onCallback) throws Exception {

        DoubleStreamingBuffer streamBuffer = new DoubleStreamingBuffer();
        ConnectorContext context = new ConnectorContext();
        context.setMetadata(metadata);
        context.setStreamBuffer(streamBuffer);
        context.setTransferId(transferId);

        TransferTask recvTask = new TransferTask(inConnector, context);
        TransferTask sendTask = new TransferTask(outConnector, context);
        List<Future<Integer>> futureList = new ArrayList<>();

        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        long startTime = System.nanoTime();

        futureList.add(completionService.submit(recvTask));
        futureList.add(completionService.submit(sendTask));

        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    int futureCnt = futureList.size();
                    for (int i = 0; i < futureCnt; i++) {
                        Future<Integer> ft = completionService.take();
                        futureList.remove(ft);
                        try {
                            ft.get();
                        } catch (InterruptedException e) {
                            // Interrupted
                            logger.error("Transfer task interrupted", e);
                        } catch (ExecutionException e) {
                            // Snap, something went wrong in the task! Abort! Abort! Abort!
                            logger.error("One task failed with error", e);

                            onCallback.accept(transferId, new TransferState()
                                .setPercentage(0)
                                .setState("FAILED")
                                .setUpdateTimeMils(System.currentTimeMillis())
                                .setDescription("Transfer failed due to " + ExceptionUtils.getStackTrace(e)));
                            for (Future<Integer> f : futureList) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    logger.error("Sleep failed", e);
                                }
                                f.cancel(true);
                            }
                            futureList.clear();
                        }
                    }

                    long endTime = System.nanoTime();

                    double time = (endTime - startTime) * 1.0 /1000000000;
                    onCallback.accept(transferId, new TransferState()
                        .setPercentage(100)
                        .setState("COMPLETED")
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setDescription("Transfer successfully completed"));
                    logger.info("Transfer Speed " + (metadata.getResourceSize() * 1.0 / time) / (1024 * 1024) + " MB/s");
                    logger.info("Transfer " + transferId + " completed");
                } catch (Exception e) {
                    logger.error("Transfer {} failed", transferId, e);
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
