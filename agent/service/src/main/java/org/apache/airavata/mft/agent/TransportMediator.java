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
import org.apache.airavata.mft.agent.stub.EndpointPaths;
import org.apache.airavata.mft.agent.transport.ConnectorResolver;
import org.apache.airavata.mft.agent.transport.TransportClassLoaderCache;
import org.apache.airavata.mft.core.api.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.UUID;
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
    private final boolean doChunkStreaming;

    private final ExecutorService chunkedExecutorService;

    public TransportMediator(String tempDataDir,
                             int concurrentTransfers,
                             int concurrentChunkedThreads,
                             int chunkedSize,
                             boolean doChunkStreaming) {
        this.tempDataDir = tempDataDir;
        monitorPool = Executors.newFixedThreadPool(concurrentTransfers);
        this.chunkedSize = chunkedSize;
        chunkedExecutorService = Executors.newFixedThreadPool(concurrentChunkedThreads);
        this.doChunkStreaming = doChunkStreaming;
    }

    public void transferSingleThread(String transferId,
                                     ConnectorConfig srcCC,
                                     ConnectorConfig dstCC,
                                     TransportClassLoaderCache transportCache,
                                     BiConsumer<EndpointPaths, TransferState> onStatusCallback,
                                     BiConsumer<String, Boolean> exitingCallback) {

        final AtomicBoolean transferInProgress = new AtomicBoolean(true);

        EndpointPaths endpointPath = EndpointPaths.newBuilder()
                .setSourcePath(srcCC.getResourcePath())
                .setDestinationPath(dstCC.getResourcePath()).build();
        try {

            logger.info("Stating transfer {}", transferId);

            Optional<IncomingStreamingConnector> inStreamingConnectorOp = ConnectorResolver
                    .resolveIncomingStreamingConnector(srcCC.getStorage().getStorageCase().name(), transportCache);
            Optional<OutgoingStreamingConnector> outStreamingConnectorOp = ConnectorResolver
                    .resolveOutgoingStreamingConnector(dstCC.getStorage().getStorageCase().name(), transportCache);

            Optional<IncomingChunkedConnector> inChunkedConnectorOp = ConnectorResolver
                    .resolveIncomingChunkedConnector(srcCC.getStorage().getStorageCase().name(), transportCache);
            Optional<OutgoingChunkedConnector> outChunkedConnectorOp = ConnectorResolver
                    .resolveOutgoingChunkedConnector(dstCC.getStorage().getStorageCase().name(), transportCache);



            onStatusCallback.accept(endpointPath, new TransferState()
                    .setPercentage(0)
                    .setState("RUNNING")
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription("Transfer is ongoing"));

            long start = System.currentTimeMillis();

            // Give priority for chunked transfers.
            // TODO: Provide a preference at the API level
            if (inChunkedConnectorOp.isPresent() && outChunkedConnectorOp.isPresent()) {

                logger.info("Starting the chunked transfer for transfer {}", transferId);

                long chunkSize = chunkedSize * 1024 * 1024L;

                CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(chunkedExecutorService);

                long fileLength = srcCC.getMetadata().getFile().getResourceSize();
                long uploadLength = 0L;
                int chunkIdx = 0;

                IncomingChunkedConnector inConnector = inChunkedConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an in chunked connector for type " + srcCC.getStorage().getStorageCase().name()));

                OutgoingChunkedConnector outConnector = outChunkedConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an out chunked connector for type " + dstCC.getStorage().getStorageCase().name()));

                inConnector.init(srcCC);
                outConnector.init(dstCC);

                try {
                    while (uploadLength < fileLength) {

                        long endPos = uploadLength + chunkSize;
                        if (endPos > fileLength) {
                            endPos = fileLength;
                        }


                        completionService.submit(new ChunkMover(inConnector,
                                outConnector, uploadLength, endPos, chunkIdx,
                                transferId, doChunkStreaming));

                        uploadLength = endPos;
                        chunkIdx++;
                    }


                    for (int i = 0; i < chunkIdx; i++) {
                        Future<Integer> future = completionService.take();
                        future.get();
                    }

                    inConnector.complete();
                    outConnector.complete();
                    logger.info("Completed chunked transfer for transfer {}", transferId);

                } catch (Exception e) {
                    inConnector.failed();
                    outConnector.failed();
                    throw e;
                }
            } else if (inStreamingConnectorOp.isPresent() && outStreamingConnectorOp.isPresent()) {

                logger.info("Starting streaming transfer for transfer {}", transferId);
                IncomingStreamingConnector inConnector = inStreamingConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an in streaming connector for type " + srcCC.getStorage().getStorageCase().name()));

                OutgoingStreamingConnector outConnector = outStreamingConnectorOp
                        .orElseThrow(() -> new Exception("Could not find an out streaming connector for type " + dstCC.getStorage().getStorageCase().name()));

                inConnector.init(srcCC);
                outConnector.init(dstCC);

                try {

                    InputStream inputStream = inConnector.fetchInputStream();
                    OutputStream outputStream = outConnector.fetchOutputStream();

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
                            double transferPercentage = countAtomic.get() * 100.0 / srcCC.getMetadata().getFile().getResourceSize();
                            logger.info("Transfer percentage for transfer {} {}", transferId, transferPercentage);
                            onStatusCallback.accept(endpointPath, new TransferState()
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

                    logger.info("Completed streaming transfer for transfer {}", transferId);
                } catch (Exception e) {
                    inConnector.failed();
                    outConnector.failed();
                    throw e;
                }

            } else {
                throw new Exception("No matching connector found to perform the transfer");
            }

            long endTime = System.currentTimeMillis();

            double time = (endTime - start) / 1000.0;

            logger.info("Transfer {} completed. Time {} S.  Speed {} MB/s", transferId, time,
                    (srcCC.getMetadata().getFile().getResourceSize() * 1.0 / time) / (1024 * 1024));

            onStatusCallback.accept(endpointPath, new TransferState()
                    .setPercentage(100)
                    .setState("COMPLETED")
                    .setUpdateTimeMils(endTime)
                    .setDescription("Transfer successfully completed"));

            exitingCallback.accept(transferId, true);
        } catch (Exception e) {

            logger.error("Transfer {} failed with error", transferId, e);

            onStatusCallback.accept(endpointPath, new TransferState()
                    .setPercentage(0)
                    .setState("FAILED")
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription("Transfer failed due to " + ExceptionUtils.getStackTrace(e)));
            exitingCallback.accept(transferId, false);
        } finally {
            transferInProgress.set(false);
        }

    }

    public void destroy() {
        monitorPool.shutdown();
    }

    private class ChunkMover implements Callable<Integer> {

        IncomingChunkedConnector downloader;
        OutgoingChunkedConnector uploader;
        long startPos;
        long endPos;
        int chunkIdx;
        String transferId;
        boolean useStreaming;

        public ChunkMover(IncomingChunkedConnector downloader, OutgoingChunkedConnector uploader, long startPos,
                          long endPos, int chunkIdx, String transferId, boolean useStreaming) {
            this.downloader = downloader;
            this.uploader = uploader;
            this.startPos = startPos;
            this.endPos = endPos;
            this.chunkIdx = chunkIdx;
            this.transferId = transferId;
            this.useStreaming = useStreaming;
        }

        @Override
        public Integer call() throws Exception {
            try {
                if (useStreaming) {
                    InputStream inputStream = downloader.downloadChunk(chunkIdx, startPos, endPos);
                    uploader.uploadChunk(chunkIdx, startPos, endPos, inputStream);
                } else {
                    String tempFile = tempDataDir + File.separator + UUID.randomUUID().toString() + "-" + chunkIdx;
                    downloader.downloadChunk(chunkIdx, startPos, endPos, tempFile);
                    uploader.uploadChunk(chunkIdx, startPos, endPos, tempFile);
                    new File(tempFile).delete();
                }
                return chunkIdx;
            } catch (Exception e) {
                logger.error("Failed to transfer transfer id {}", transferId, e);
                throw e;
            }
        }
    }
}
