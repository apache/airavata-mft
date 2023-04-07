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

import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.agent.transport.MetadataCollectorResolver;
import org.apache.airavata.mft.agent.transport.TransportClassLoaderCache;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

public class TransferOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(TransferOrchestrator.class);

    private final AtomicLong totalRunningTransfers = new AtomicLong(0);
    private final AtomicLong totalPendingTransfers = new AtomicLong(0);

    @org.springframework.beans.factory.annotation.Value("${agent.concurrent.transfers}")
    private int concurrentTransfers;

    private ExecutorService transferRequestExecutor;

    private TransportMediator mediator;

    @org.springframework.beans.factory.annotation.Value("${agent.concurrent.chunked.threads}")
    private int concurrentChunkedThreads;

    @org.springframework.beans.factory.annotation.Value("${agent.chunk.size}")
    private int chunkedSize;

    @org.springframework.beans.factory.annotation.Value("${agent.chunk.streaming.enabled}")
    private boolean doChunkStream;

    @org.springframework.beans.factory.annotation.Value("${agent.temp.data.dir}")
    private String tempDataDir = "/tmp";

    @org.springframework.beans.factory.annotation.Value("${agent.id}")
    private String agentId;

    @Autowired
    private MFTConsulClient mftConsulClient;

    @Autowired
    private TransportConfig transportConfig;

    @PostConstruct
    public void init() {
        transferRequestExecutor  = Executors.newFixedThreadPool(concurrentTransfers);
        mediator = new TransportMediator(tempDataDir,
                concurrentTransfers,
                concurrentChunkedThreads,
                chunkedSize, doChunkStream);
        mftConsulClient.updateAgentPendingTransferCount(agentId, 0);
        logger.info("Transfer orchestrator initialized");
    }

    @PreDestroy
    public void destroy() {
        transferRequestExecutor.shutdown();
        logger.info("Transfer orchestrator turned off");
    }

    public void submitTransferToProcess(String transferId, AgentTransferRequest request, TransportClassLoaderCache cache,
                                        BiConsumer<EndpointPaths, TransferState> updateStatus,
                                        BiConsumer<EndpointPaths, Boolean> createTransferHook) {
        long totalPending = totalPendingTransfers.addAndGet(request.getEndpointPathsCount());
        mftConsulClient.updateAgentPendingTransferCount(agentId, totalPending);

        logger.info("Total pending files to transfer {}", totalPending);
        for (EndpointPaths endpointPath : request.getEndpointPathsList()) {

            transferRequestExecutor.submit(() -> processTransfer(transferId, request.getRequestId(),
                    request.getSourceStorage(),
                    request.getDestinationStorage(), request.getSourceSecret(),
                    request.getDestinationSecret(), endpointPath, cache,
                    updateStatus, createTransferHook));
        }
    }

    public void processTransfer(String transferId, String requestId, StorageWrapper sourceStorage, StorageWrapper destStorage,
                                SecretWrapper sourceSecret,SecretWrapper destSecret, EndpointPaths endpointPath,
                                TransportClassLoaderCache transportCache,
                                BiConsumer<EndpointPaths, TransferState> updateStatus,
                                BiConsumer<EndpointPaths, Boolean> createTransferHook) {
        try {

            long running = totalRunningTransfers.incrementAndGet();
            long pending = totalPendingTransfers.decrementAndGet();
            mftConsulClient.updateAgentPendingTransferCount(agentId, pending);

            logger.info("Received request {}. Total Running {}. Total Pending {}", transferId, running, pending);

            updateStatus.accept(endpointPath, new TransferState()
                    .setState("STARTING")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription("Starting the transfer"));

            Optional<MetadataCollector> srcMetadataCollectorOp = MetadataCollectorResolver
                    .resolveMetadataCollector(sourceStorage.getStorageCase().name(), transportCache);

            MetadataCollector srcMetadataCollector = srcMetadataCollectorOp.orElseThrow(() -> new Exception("Could not find a metadata collector for source"));
            srcMetadataCollector.init(sourceStorage, sourceSecret);

            ResourceMetadata srcMetadata = srcMetadataCollector.getResourceMetadata(endpointPath.getSourcePath(), false);
            if (srcMetadata.getMetadataCase() != ResourceMetadata.MetadataCase.FILE) {
                throw new Exception("Expected a file as the source but received " + srcMetadata.getMetadataCase().name());
            }

            Optional<MetadataCollector> dstMetadataCollectorOp = MetadataCollectorResolver
                    .resolveMetadataCollector(destStorage.getStorageCase().name(), transportCache);

            MetadataCollector dstMetadataCollector = dstMetadataCollectorOp.orElseThrow(() -> new Exception("Could not find a metadata collector for destination"));
            dstMetadataCollector.init(destStorage, destSecret);

            if (dstMetadataCollector.isAvailable(endpointPath.getDestinationPath())) {
                ResourceMetadata destinationMetadata = dstMetadataCollector.getResourceMetadata(endpointPath.getDestinationPath(), false);
                if (destinationMetadata.getMetadataCase() == ResourceMetadata.MetadataCase.FILE &&
                        destinationMetadata.getFile().getResourceSize() == srcMetadata.getFile().getResourceSize()) {
                    logger.info("Ignoring the transfer of file {} as it is available in the destination", endpointPath.getSourcePath());
                    updateStatus.accept(endpointPath, new TransferState()
                            .setPercentage(100)
                            .setState("COMPLETED")
                            .setUpdateTimeMils(System.currentTimeMillis())
                            .setDescription("Ignoring transfer as the file is available in destination"));

                    return;
                }
            }

            ConnectorConfig srcCC = ConnectorConfig.ConnectorConfigBuilder.newBuilder()
                    .withTransferId(transferId)
                    .withSecret(sourceSecret)
                    .withStorage(sourceStorage)
                    .withResourcePath(endpointPath.getSourcePath())
                    .withChunkSize(chunkedSize)
                    .withTransportConfig(transportConfig.getTransport())
                    .withMetadata(srcMetadata).build();

            ConnectorConfig dstCC = ConnectorConfig.ConnectorConfigBuilder.newBuilder()
                    .withTransferId(transferId)
                    .withStorage(destStorage)
                    .withSecret(destSecret)
                    .withResourcePath(endpointPath.getDestinationPath())
                    .withChunkSize(chunkedSize)
                    .withTransportConfig(transportConfig.getTransport())
                    .withMetadata(srcMetadata).build();

            updateStatus.accept(endpointPath, new TransferState()
                    .setState("STARTED")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription("Started the transfer"));

            // Save transfer metadata in scheduled path to recover in case of an Agent failures. Recovery is done from controller
            createTransferHook.accept(endpointPath, true);

            mediator.transferSingleThread(transferId, srcCC, dstCC, transportCache, updateStatus,
                    (id, transferSuccess) -> {
                        try {
                            // Delete scheduled key as the transfer completed / failed if it was placed in current session
                            createTransferHook.accept(endpointPath,false);
                            long pendingAfter = totalRunningTransfers.decrementAndGet();
                            logger.info("Removed transfer {} from queue with transfer success = {}. Total running {}",
                                    id, transferSuccess, pendingAfter);
                        } catch (Exception e) {
                            logger.error("Failed while deleting scheduled path for transfer {}", id);
                        }
                    });


        } catch (Throwable e) {
            logger.error("Error in submitting transfer {}", transferId, e);

            updateStatus.accept(endpointPath, new TransferState()
                    .setState("FAILED")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription(ExceptionUtils.getStackTrace(e)));


        } finally {
            //logger.info("Deleting key " + consulEntryKey);
            //mftConsulClient.getKvClient().deleteKey(consulEntryKey); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
        }
    }

}
