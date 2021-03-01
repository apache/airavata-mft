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
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.core.*;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class TransportMediator {

    private static final Logger logger = LoggerFactory.getLogger(TransportMediator.class);

    /*
    Number of maximum transfers handled at atime
     */
    private int concurrentTransfers = 10;
    private ExecutorService executor = Executors.newFixedThreadPool(concurrentTransfers * 2); // 2 connections per transfer
    private ExecutorService monitorPool = Executors.newFixedThreadPool(concurrentTransfers * 2); // 2 monitors per transfer

    public void destroy() {
        executor.shutdown();
    }

    public void transfer(String transferId, TransferApiRequest request, Connector inConnector, Connector outConnector, MetadataCollector srcMetadataCollector,
                           MetadataCollector destMetadataCollector, BiConsumer<String, TransferState> onStatusCallback,
                           BiConsumer<String, Boolean> exitingCallback) throws Exception {

        FileResourceMetadata srcMetadata = srcMetadataCollector.getFileResourceMetadata(
                            request.getMftAuthorizationToken(),
                            request.getSourceResourceId(),
                            request.getSourceToken());

        final long resourceSize = srcMetadata.getResourceSize();
        logger.debug("Source file size {}. MD5 {}", resourceSize, srcMetadata.getMd5sum());

        final DoubleStreamingBuffer streamBuffer = new DoubleStreamingBuffer();
        final ReentrantLock statusLock = new ReentrantLock();

        ConnectorContext context = new ConnectorContext();
        context.setMetadata(srcMetadata);
        context.setStreamBuffer(streamBuffer);
        context.setTransferId(transferId);

        TransferTask recvTask = new TransferTask(request.getMftAuthorizationToken(), request.getSourceResourceId(),
                request.getSourceToken(), context, inConnector);
        TransferTask sendTask = new TransferTask(request.getMftAuthorizationToken(), request.getDestinationResourceId(),
                request.getDestinationToken(), context, outConnector);
        List<Future<Integer>> futureList = new ArrayList<>();

        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);

        long startTime = System.nanoTime();

        futureList.add(completionService.submit(recvTask));
        futureList.add(completionService.submit(sendTask));

        final AtomicBoolean transferInProgress = new AtomicBoolean(true);
        final AtomicBoolean transferSuccess = new AtomicBoolean(true);


        // Monitoring the completeness of the transfer
        Thread monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    int futureCnt = futureList.size();
                    boolean transferErrored = false;

                    for (int i = 0; i < futureCnt; i++) {
                        Future<Integer> ft = completionService.take();
                        futureList.remove(ft);
                        try {
                            ft.get();
                        } catch (Exception e) {

                            logger.error("One task failed with error", e);
                            transferErrored = true;
                            statusLock.lock();
                            onStatusCallback.accept(transferId, new TransferState()
                                .setPercentage(0)
                                .setState("FAILED")
                                .setUpdateTimeMils(System.currentTimeMillis())
                                .setDescription("Transfer failed due to " + ExceptionUtils.getStackTrace(e)));
                            transferInProgress.set(false);
                            transferSuccess.set(false);
                            statusLock.unlock();

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

                    if (!transferErrored) {
                        Boolean transferred = destMetadataCollector.isAvailable(
                                request.getMftAuthorizationToken(),
                                request.getDestinationResourceId(),
                                request.getDestinationToken());


                        if (!transferred) {
                            logger.error("Transfer completed but resource is not available in destination");
                            throw new Exception("Transfer completed but resource is not available in destination");
                        }

                        FileResourceMetadata destMetadata = destMetadataCollector.getFileResourceMetadata(
                                request.getMftAuthorizationToken(),
                                request.getDestinationResourceId(),
                                request.getDestinationToken());


                        boolean doIntegrityVerify = true;

                        if (srcMetadata.getMd5sum() == null) {
                            logger.warn("MD5 sum is not available for source resource. So this disables integrity verification");
                            doIntegrityVerify = false;
                        } else if (destMetadata.getMd5sum() == null) {
                            logger.warn("MD5 sum is not available for destination resource. So this disables integrity verification");
                            doIntegrityVerify = false;
                        }

                        if (doIntegrityVerify && !destMetadata.getMd5sum().equals(srcMetadata.getMd5sum())) {
                            logger.error("Resource integrity violated. MD5 sums are not matching. Source md5 {} destination md5 {}",
                                    srcMetadata.getMd5sum(), destMetadata.getMd5sum());
                            throw new Exception("Resource integrity violated. MD5 sums are not matching. Source md5 " + srcMetadata.getMd5sum()
                                    + " destination md5 " + destMetadata.getMd5sum());
                        }

                        // Check

                        long endTime = System.nanoTime();

                        double time = (endTime - startTime) * 1.0 / 1000000000;

                        statusLock.lock();
                        onStatusCallback.accept(transferId, new TransferState()
                                .setPercentage(100)
                                .setState("COMPLETED")
                                .setUpdateTimeMils(System.currentTimeMillis())
                                .setDescription("Transfer successfully completed"));
                        transferInProgress.set(false);
                        transferSuccess.set(true);
                        statusLock.unlock();

                        logger.info("Transfer {} completed.  Speed {} MB/s", transferId,
                                (srcMetadata.getResourceSize() * 1.0 / time) / (1024 * 1024));
                    }
                } catch (Exception e) {

                    statusLock.lock();
                    onStatusCallback.accept(transferId, new TransferState()
                            .setPercentage(0)
                            .setState("FAILED")
                            .setUpdateTimeMils(System.currentTimeMillis())
                            .setDescription("Transfer failed due to " + ExceptionUtils.getStackTrace(e)));
                    transferInProgress.set(false);
                    transferSuccess.set(false);
                    statusLock.unlock();

                    logger.error("Transfer {} failed", transferId, e);
                } finally {
                    inConnector.destroy();
                    outConnector.destroy();
                    transferInProgress.set(false);
                    exitingCallback.accept(transferId,transferSuccess.get());
                }
            }
        });

        // Monitoring the status of the transfer
        Thread progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    statusLock.lock();
                    if (!transferInProgress.get()){
                        statusLock.unlock();
                        logger.info("Status monitor is exiting for transfer {}", transferId);
                        break;
                    }
                    double transferPercentage = streamBuffer.getProcessedBytes() * 100.0/ resourceSize;
                    logger.info("Transfer percentage for transfer {} {}", transferId, transferPercentage);
                    onStatusCallback.accept(transferId, new TransferState()
                            .setPercentage(transferPercentage)
                            .setState("RUNNING")
                            .setUpdateTimeMils(System.currentTimeMillis())
                            .setDescription("Transfer Progress Updated"));
                    statusLock.unlock();
                }
            }
        });

        monitorPool.submit(monitorThread);
        monitorPool.submit(progressThread);
    }
}
