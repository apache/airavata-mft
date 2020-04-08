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

import org.apache.airavata.mft.admin.models.TransferCommand;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.core.*;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
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

    public String transfer(TransferCommand command, Connector inConnector, Connector outConnector, MetadataCollector srcMetadataCollector,
                           MetadataCollector destMetadataCollector, BiConsumer<String, TransferState> onCallback) throws Exception {

        ResourceMetadata srcMetadata = srcMetadataCollector.getGetResourceMetadata(command.getSourceId(), command.getSourceToken());

        logger.debug("Source file size " + srcMetadata.getResourceSize() + ". MD5 " + srcMetadata.getMd5sum());

        DoubleStreamingBuffer streamBuffer = new DoubleStreamingBuffer();
        ConnectorContext context = new ConnectorContext();
        context.setMetadata(srcMetadata);
        context.setStreamBuffer(streamBuffer);
        context.setTransferId(command.getTransferId());

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

                            logger.error("Transfer task interrupted", e);
                        } catch (ExecutionException e) {

                            logger.error("One task failed with error", e);

                            onCallback.accept(command.getTransferId(), new TransferState()
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

                    Boolean transferred = destMetadataCollector.isAvailable(command.getDestinationId(), command.getDestinationToken());

                    if (!transferred) {
                        logger.error("Transfer completed but resource is not available in destination");
                        throw new Exception("Transfer completed but resource is not available in destination");
                    }

                    ResourceMetadata destMetadata = destMetadataCollector.getGetResourceMetadata(command.getDestinationId(),
                                                    command.getDestinationToken());

                    if (destMetadata.getMd5sum().equals(srcMetadata.getMd5sum())) {
                        logger.error("Resource integrity violated. MD5 sums are not matching. Source md5 {} destination md5 {}",
                                                            srcMetadata.getMd5sum(), destMetadata.getMd5sum());
                        throw new Exception("Resource integrity violated. MD5 sums are not matching. Source md5 " + srcMetadata.getMd5sum()
                                                        + " destination md5 " + destMetadata.getMd5sum());
                    }

                    // Check

                    long endTime = System.nanoTime();

                    double time = (endTime - startTime) * 1.0 /1000000000;
                    onCallback.accept(command.getTransferId(), new TransferState()
                        .setPercentage(100)
                        .setState("COMPLETED")
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setDescription("Transfer successfully completed"));

                    logger.info("Transfer {} completed.  Speed {} MB/s", command.getTransferId(),
                                                    (srcMetadata.getResourceSize() * 1.0 / time) / (1024 * 1024));

                } catch (Exception e) {

                    onCallback.accept(command.getTransferId(), new TransferState()
                            .setPercentage(0)
                            .setState("FAILED")
                            .setUpdateTimeMils(System.currentTimeMillis())
                            .setDescription("Transfer failed due to " + ExceptionUtils.getStackTrace(e)));

                    logger.error("Transfer {} failed", command.getTransferId(), e);
                } finally {
                    inConnector.destroy();
                    outConnector.destroy();
                }
            }
        });

        monitor.submit(monitorThread);
        return command.getTransferId();
    }
}
