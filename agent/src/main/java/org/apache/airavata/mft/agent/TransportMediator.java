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

import java.io.File;
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
    private final ExecutorService monitorPool;

    private String tempDataDir = "/tmp";
    private final int chunkedSize;

    private final ExecutorService chunkedExecutorService;

    public TransportMediator(String tempDataDir, int concurrentTransfers, int concurrentChunkedThreads, int chunkedSize) {
        this.tempDataDir = tempDataDir;
        monitorPool = Executors.newFixedThreadPool(concurrentTransfers);
        this.chunkedSize = chunkedSize;
        chunkedExecutorService = Executors.newFixedThreadPool(concurrentChunkedThreads);
    }

    public void transferSingleThread(String transferId,
                                     TransferApiRequest request,
                                     ConnectorConfig srcCC,
                                     ConnectorConfig dstCC,
                                     BiConsumer<String, TransferState> onStatusCallback,
                                     BiConsumer<String, Boolean> exitingCallback) {

        final AtomicBoolean transferInProgress = new AtomicBoolean(true);

        try {

            logger.info("Stating transfer {}", transferId);
            long start = System.currentTimeMillis();

            onStatusCallback.accept(transferId, new TransferState()
                    .setPercentage(0)
                    .setState("RUNNING")
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription("Transfer is ongoing"));

            Optional<IncomingStreamingConnector> inStreamingConnectorOp = ConnectorResolver
                    .resolveIncomingStreamingConnector(request.getSourceType());
            Optional<OutgoingStreamingConnector> outStreamingConnectorOp = ConnectorResolver
                    .resolveOutgoingStreamingConnector(request.getDestinationType());

            Optional<IncomingChunkedConnector> inChunkedConnectorOp = ConnectorResolver
                    .resolveIncomingChunkedConnector(request.getSourceType());
            Optional<OutgoingChunkedConnector> outChunkedConnectorOp = ConnectorResolver
                    .resolveOutgoingChunkedConnector(request.getDestinationType());

            // Give priority for chunked transfers.
            // TODO: Provide a preference at the API level
            if (inChunkedConnectorOp.isPresent() && outChunkedConnectorOp.isPresent()) {

                logger.info("Starting the chunked transfer for transfer {}", transferId);

                long chunkSize = chunkedSize * 1024 * 1024L;

                CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(chunkedExecutorService);

                long fileLength = srcCC.getMetadata().getResourceSize();
                long uploadLength = 0L;
                int chunkIdx = 0;

                IncomingChunkedConnector inConnector = inChunkedConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an in chunked connector for type " + request.getSourceType()));

                OutgoingChunkedConnector outConnector = outChunkedConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an out chunked connector for type " + request.getDestinationType()));

                inConnector.init(srcCC);
                outConnector.init(dstCC);

                while(uploadLength < fileLength) {

                    long endPos = uploadLength + chunkSize;
                    if (endPos > fileLength) {
                        endPos = fileLength;
                    }

                    String tempFile = tempDataDir + File.separator + transferId + "-" + chunkIdx;
                    completionService.submit(new ChunkMover(inConnector, outConnector, uploadLength, endPos, chunkIdx, tempFile));

                    uploadLength = endPos;
                    chunkIdx++;
                }


                for (int i = 0; i < chunkIdx; i++) {
                    Future<Integer> future = completionService.take();
                }

                inConnector.complete();
                outConnector.complete();
                logger.info("Completed chunked transfer for transfer {}", transferId);

            } else if (inStreamingConnectorOp.isPresent() && outStreamingConnectorOp.isPresent()) {

                logger.info("Starting streaming transfer for transfer {}", transferId);
                IncomingStreamingConnector inConnector = inStreamingConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an in streaming connector for type " + request.getSourceType()));

                OutgoingStreamingConnector outConnector = outStreamingConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an out streaming connector for type " + request.getDestinationType()));

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
                        double transferPercentage = countAtomic.get() * 100.0 / srcCC.getMetadata().getResourceSize();
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
                for (count = 0L; -1 != (n = inputStream.read(buffer)); count += (long) n) {
                    outputStream.write(buffer, 0, n);
                    countAtomic.set(count);
                }

                inConnector.complete();
                outConnector.complete();

                logger.info("Completed streaming ransfer for transfer {}", transferId);

            } else {
                throw new Exception("No matching connector found to perform the transfer");
            }

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

    }

    public void destroy() {
        monitorPool.shutdown();
    }

    private static class ChunkMover implements Callable<Integer> {

        IncomingChunkedConnector downloader;
        OutgoingChunkedConnector uploader;
        long startPos;
        long endPos;
        int chunkIdx;
        String tempFile;

        public ChunkMover(IncomingChunkedConnector downloader, OutgoingChunkedConnector uploader, long startPos,
                          long endPos, int chunkIdx, String tempFile) {
            this.downloader = downloader;
            this.uploader = uploader;
            this.startPos = startPos;
            this.endPos = endPos;
            this.chunkIdx = chunkIdx;
            this.tempFile = tempFile;
        }

        @Override
        public Integer call() throws Exception {
            downloader.downloadChunk(chunkIdx, startPos, endPos, tempFile);
            uploader.uploadChunk(chunkIdx, startPos, endPos, tempFile);
            new File(tempFile).delete();
            return chunkIdx;
        }
    }
}
