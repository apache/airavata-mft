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
import org.apache.airavata.mft.core.api.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public class TransportMediator {

    private static final Logger logger = LoggerFactory.getLogger(TransportMediator.class);

    /*
    Number of maximum transfers handled at atime
     */
    private final int concurrentTransfers = 10;
    private final ExecutorService executor = Executors.newFixedThreadPool(concurrentTransfers * 2); // 2 connections per transfer
    private final ExecutorService monitorPool = Executors.newFixedThreadPool(concurrentTransfers * 2); // 2 monitors per transfer

    public void transferSingleThread(String transferId,
                                     TransferApiRequest request,
                                     ConnectorConfig srcCC,
                                     ConnectorConfig dstCC,
                                     BiConsumer<String, TransferState> onStatusCallback,
                                     BiConsumer<String, Boolean> exitingCallback) {

        executor.submit(() -> {

            final AtomicBoolean transferInProgress = new AtomicBoolean(true);

            try {

                long start = System.currentTimeMillis();

                onStatusCallback.accept(transferId, new TransferState()
                        .setPercentage(100)
                        .setState("RUNNING")
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setDescription("Transfer successfully completed"));

                Optional<IncomingConnector> inConnectorOp = ConnectorResolver.resolveIncomingConnector(request.getSourceType());
                Optional<OutgoingConnector> outConnectorOp = ConnectorResolver.resolveOutgoingConnector(request.getDestinationType());

                IncomingConnector inConnector = inConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an in connector for type " + request.getSourceType()));

                OutgoingConnector outConnector = outConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an out connector for type " + request.getDestinationType()));

                inConnector.init(srcCC);
                outConnector.init(dstCC);

                String srcChild = request.getSourceChildResourcePath();
                String dstChild = request.getDestinationChildResourcePath();

                InputStream inputStream = srcChild.equals("") ? inConnector.fetchInputStream() : inConnector.fetchInputStream(srcChild);
                OutputStream outputStream = dstChild.equals("") ? outConnector.fetchOutputStream() : outConnector.fetchOutputStream(dstChild);

                long count = 0;
                final AtomicLong countAtomic = new AtomicLong();
                countAtomic.set(count);

                monitorPool.submit(() -> {
                    while (true) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                        if (!transferInProgress.get()) {
                            logger.info("Status monitor is exiting for transfer {}", transferId);
                            break;
                        }
                        double transferPercentage = countAtomic.get() * 100.0/ srcCC.getMetadata().getResourceSize();
                        logger.info("Transfer percentage for transfer {} {}", transferId, transferPercentage);
                        onStatusCallback.accept(transferId, new TransferState()
                                .setPercentage(transferPercentage)
                                .setState("RUNNING")
                                .setUpdateTimeMils(System.currentTimeMillis())
                                .setDescription("Transfer Progress Updated"));
                    }
                });

                int n;
                byte[] buffer = new byte[128 * 1024];
                for(count = 0L; -1 != (n = inputStream.read(buffer)); count += (long)n) {
                    outputStream.write(buffer, 0, n);
                    countAtomic.set(count);
                }

                inConnector.complete();
                outConnector.complete();

                long time = (System.currentTimeMillis() - start) / 1000;

                logger.info("Transfer {} completed. Time {} S.  Speed {} MB/s", transferId, time,
                        (srcCC.getMetadata().getResourceSize() * 1.0 / time) / (1024 * 1024));

                onStatusCallback.accept(transferId, new TransferState()
                        .setPercentage(100)
                        .setState("COMPLETED")
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setDescription("Transfer successfully completed"));

                exitingCallback.accept(transferId, true);
            } catch (Exception e) {

                logger.error("Transfer {} failed with error", transferId, e);

                onStatusCallback.accept(transferId, new TransferState()
                        .setPercentage(0)
                        .setState("FAILED")
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setDescription("Transfer failed due to " + ExceptionUtils.getStackTrace(e)));
            } finally {
                transferInProgress.set(false);
            }
        });
    }

    public void destroy() {
        executor.shutdown();
        monitorPool.shutdown();
    }
}
