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
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageTypeResolveRequest;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageTypeResolveResponse;
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
import java.util.function.Consumer;

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

    @org.springframework.beans.factory.annotation.Value("${resource.service.host}")
    private String resourceServiceHost;

    @org.springframework.beans.factory.annotation.Value("${resource.service.port}")
    private int resourceServicePort;

    @org.springframework.beans.factory.annotation.Value("${secret.service.host}")
    private String secretServiceHost;

    @org.springframework.beans.factory.annotation.Value("${secret.service.port}")
    private int secretServicePort;

    @Autowired
    private StorageServiceClient storageServiceClient;

    @PostConstruct
    public void init() {
        transferRequestExecutor  = Executors.newFixedThreadPool(concurrentTransfers);
        mediator = new TransportMediator(tempDataDir,
                concurrentTransfers,
                concurrentChunkedThreads,
                chunkedSize, doChunkStream);
        logger.info("Transfer orchestrator initialized");
    }

    @PreDestroy
    public void destroy() {
        transferRequestExecutor.shutdown();
        logger.info("Transfer orchestrator turned off");
    }

    public void submitTransferToProcess(String transferId, TransferApiRequest request,
                                        BiConsumer<String, TransferState> updateStatus,
                                        Consumer<Boolean> createTransferHook) {
        long totalPending = totalPendingTransfers.incrementAndGet();
        logger.info("Total pending transfers {}", totalPending);
        transferRequestExecutor.submit(() -> processTransfer(transferId, request, updateStatus, createTransferHook));
    }

    public void processTransfer(String transferId, TransferApiRequest request,
                                BiConsumer<String, TransferState> updateStatus, Consumer<Boolean> createTransferHook) {
        try {

            long running = totalRunningTransfers.incrementAndGet();
            long pending = totalPendingTransfers.decrementAndGet();
            logger.info("Received request {}. Total Running {}. Total Pending {}", transferId, running, pending);

            updateStatus.accept(transferId, new TransferState()
                    .setState("STARTING")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription("Starting the transfer"));

            StorageTypeResolveResponse sourceStorageType = storageServiceClient.common()
                    .resolveStorageType(StorageTypeResolveRequest.newBuilder()
                            .setStorageId(request.getSourceStorageId()).build());
            Optional<MetadataCollector> srcMetadataCollectorOp = MetadataCollectorResolver
                    .resolveMetadataCollector(sourceStorageType.getStorageType());
            MetadataCollector srcMetadataCollector = srcMetadataCollectorOp.orElseThrow(() -> new Exception("Could not find a metadata collector for source"));
            srcMetadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);

            StorageTypeResolveResponse destStorageType = storageServiceClient.common()
                    .resolveStorageType(StorageTypeResolveRequest.newBuilder()
                            .setStorageId(request.getSourceStorageId()).build());

            FileResourceMetadata srcMetadata = srcMetadataCollector.getFileResourceMetadata(
                    request.getMftAuthorizationToken(),
                    request.getSourcePath(),
                    request.getSourceStorageId(),
                    request.getSourceToken());


            ConnectorConfig srcCC = ConnectorConfig.ConnectorConfigBuilder.newBuilder()
                    .withAuthToken(request.getMftAuthorizationToken())
                    .withResourceServiceHost(resourceServiceHost)
                    .withResourceServicePort(resourceServicePort)
                    .withSecretServiceHost(secretServiceHost)
                    .withSecretServicePort(secretServicePort)
                    .withTransferId(transferId)
                    .withStorageId(request.getSourceStorageId())
                    .withResourcePath(request.getSourcePath())
                    .withStorageType(sourceStorageType.getStorageType())
                    .withCredentialToken(request.getSourceToken())
                    .withMetadata(srcMetadata).build();

            ConnectorConfig dstCC = ConnectorConfig.ConnectorConfigBuilder.newBuilder()
                    .withAuthToken(request.getMftAuthorizationToken())
                    .withResourceServiceHost(resourceServiceHost)
                    .withResourceServicePort(resourceServicePort)
                    .withSecretServiceHost(secretServiceHost)
                    .withSecretServicePort(secretServicePort)
                    .withTransferId(transferId)
                    .withStorageId(request.getDestinationStorageId())
                    .withResourcePath(request.getDestinationPath())
                    .withStorageType(destStorageType.getStorageType())
                    .withCredentialToken(request.getDestinationToken())
                    .withMetadata(srcMetadata).build();

            updateStatus.accept(transferId, new TransferState()
                    .setState("STARTED")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setDescription("Started the transfer"));

            // Save transfer metadata in scheduled path to recover in case of an Agent failures. Recovery is done from controller
            createTransferHook.accept(true);

            mediator.transferSingleThread(transferId, request, srcCC, dstCC, updateStatus,
                    (id, transferSuccess) -> {
                        try {
                            // Delete scheduled key as the transfer completed / failed if it was placed in current session
                            createTransferHook.accept(false);
                            long pendingAfter = totalRunningTransfers.decrementAndGet();
                            logger.info("Removed transfer {} from queue with transfer success = {}. Total running {}",
                                    id, transferSuccess, pendingAfter);
                        } catch (Exception e) {
                            logger.error("Failed while deleting scheduled path for transfer {}", id);
                        }
                    });


        } catch (Throwable e) {
            if (request != null) {
                logger.error("Error in submitting transfer {}", transferId, e);

                updateStatus.accept(transferId, new TransferState()
                        .setState("FAILED")
                        .setPercentage(0)
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setDescription(ExceptionUtils.getStackTrace(e)));

            } else {
                logger.error("Unknown error in processing message {}", request.toString(), e);
            }
        } finally {
            //logger.info("Deleting key " + consulEntryKey);
            //mftConsulClient.getKvClient().deleteKey(consulEntryKey); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
        }
    }

}
