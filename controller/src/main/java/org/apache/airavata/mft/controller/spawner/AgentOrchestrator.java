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

package org.apache.airavata.mft.controller.spawner;

import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.agent.stub.AgentTransferRequest;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.controller.TransferDispatcher;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class AgentOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(AgentOrchestrator.class);

    private final int SPAWNER_MAX_IDLE_SECONDS = 30; // TODO Externalize this

    private class TransferInfo {
        private final String transferId;
        private final AgentTransferRequest agentTransferRequest;
        private final TransferApiRequest transferApiRequest;

        // Temporarily store consul key until the optimizer spins up Agents. This will block the same pending transfer
        // being handled twice
        private final String consulKey;

        public TransferInfo(String transferId, AgentTransferRequest agentTransferRequest, TransferApiRequest transferApiRequest, String consulKey) {
            this.transferId = transferId;
            this.agentTransferRequest = agentTransferRequest;
            this.transferApiRequest = transferApiRequest;
            this.consulKey = consulKey;
        }

        public String getTransferId() {
            return transferId;
        }

        public AgentTransferRequest getAgentTransferRequest() {
            return agentTransferRequest;
        }

        public TransferApiRequest getTransferApiRequest() {
            return transferApiRequest;
        }

        public String getConsulKey() {
            return consulKey;
        }
    }

    private class LaunchedSpawnerMetadata implements Comparable<LaunchedSpawnerMetadata> {

        private final AgentSpawner spawner;

        private final long createdTime = System.currentTimeMillis();
        private long lastScannedTime = System.currentTimeMillis();

        //AgentTransferRequestId:TransferInfo
        private final Map<String, TransferInfo> transferInfos = new ConcurrentHashMap<>();

        public LaunchedSpawnerMetadata(AgentSpawner spawner) {
            this.spawner = spawner;
        }

        public AgentSpawner getSpawner() {
            return spawner;
        }

        public Map<String, TransferInfo> getTransferInfos() {
            return transferInfos;
        }

        @Override
        public int compareTo(LaunchedSpawnerMetadata o) {
            if (createdTime == o.createdTime)
                return 0;
            return o.createdTime < createdTime? 1 : -1;
        }
    }

    private final Map<String, LaunchedSpawnerMetadata> launchedSpawnersMap = new ConcurrentHashMap<>();

    private final TransferDispatcher transferDispatcher;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public AgentOrchestrator(TransferDispatcher transferDispatcher) {
        this.transferDispatcher = transferDispatcher;
    }

    public void init() {
        scheduler.scheduleWithFixedDelay(() -> {

            try {
                launchedSpawnersMap.forEach((key, metadata) -> {
                    if (metadata.spawner.getLaunchState().isDone()) {
                        metadata.transferInfos.forEach((agentTransferId, transferInfo) -> {
                            try {
                                String agentId = metadata.spawner.getLaunchState().get();
                                List<String> liveAgentIds = transferDispatcher.getMftConsulClient().getLiveAgentIds();

                                if (liveAgentIds.stream().noneMatch(id -> id.equals(agentId))) {
                                    throw new Exception("Agent was not registered even though the agent maked as up");
                                }

                                this.transferDispatcher.submitTransferToAgent(Collections.singletonList(agentId),
                                        transferInfo.transferId,
                                        transferInfo.transferApiRequest,
                                        transferInfo.agentTransferRequest,
                                        transferInfo.consulKey);

                                metadata.lastScannedTime = System.currentTimeMillis();
                            } catch (Exception e) {
                                logger.error("Failed to launch agent for agent transfer id {} and transfer {}",
                                        agentTransferId, transferInfo.transferId, e);
                                try {
                                    transferDispatcher.getMftConsulClient().saveTransferState(transferInfo.transferId, null, new TransferState()
                                            .setUpdateTimeMils(System.currentTimeMillis())
                                            .setState("FAILED").setPercentage(0)
                                            .setPublisher("controller")
                                            .setDescription("Failed to launch the agent. " + ExceptionUtils.getRootCauseMessage(e)));
                                } catch (Exception e2) {
                                    logger.error("Failed to submit transfer fail error for transfer id {}", transferInfo.transferId, e2);
                                }

                                logger.info("Removing consul key {}", transferInfo.consulKey);
                                transferDispatcher.getMftConsulClient().getKvClient().deleteKey(transferInfo.consulKey);
                                logger.info("Terminating the spawner");
                                metadata.spawner.terminate(true);
                                launchedSpawnersMap.remove(key);
                            } finally {
                                metadata.transferInfos.remove(agentTransferId);
                            }
                        });
                    }

                    if ((System.currentTimeMillis() - metadata.lastScannedTime) >  SPAWNER_MAX_IDLE_SECONDS * 1000) {

                        if (metadata.transferInfos.size() > 0) {
                            return;
                        }

                        logger.info("No transfer infos for spawner {}. Checking for termination", key);

                        try {
                            String agentId = null;
                            try {
                                agentId = metadata.spawner.getLaunchState().get();

                            } catch (Exception e) {
                                logger.info("Killing spawner with key {} as the agent is not responding and inactive for {} seconds",
                                    key, SPAWNER_MAX_IDLE_SECONDS);
                                metadata.spawner.terminate(false);
                                launchedSpawnersMap.remove(key);
                                return;
                            }

                            List<String> pendingAgentTransfers = transferDispatcher.getMftConsulClient().listPendingAgentTransfers(agentId);
                            if (pendingAgentTransfers.isEmpty()) {
                                int totalFilesInProgress = transferDispatcher.getMftConsulClient().getEndpointHookCountForAgent(agentId);
                                if (totalFilesInProgress == 0) {
                                    logger.info("Killing spawner with key {} as all files were transferred and the agent" +
                                                    " is inactive for {} seconds",
                                            key, SPAWNER_MAX_IDLE_SECONDS);
                                    metadata.spawner.terminate(false);
                                    launchedSpawnersMap.remove(key);
                                }
                            }

                        } catch (Exception e) {
                            logger.error("Failed while fetching the endpoint count for agent", e);
                        }
                    }
                });

            } catch (Exception e) {
                // Just to keep the thread running
                logger.error("Some error occurred while processing spawners map", e);
            }

        }, 3, 5, TimeUnit.SECONDS);
    }
    public boolean tryLaunchingAgent(String transferId,
                                     TransferApiRequest transferRequest,
                                     AgentTransferRequest agentTransferRequest,
                                     String consulKey) {


        List<LaunchedSpawnerMetadata> selectedSpawnerMetadata = new ArrayList<>();

        LaunchedSpawnerMetadata sourceSpawnerMetadata = launchedSpawnersMap.get(getId(transferRequest, true));
        if (sourceSpawnerMetadata != null) {
            selectedSpawnerMetadata.add(sourceSpawnerMetadata);
        }

        LaunchedSpawnerMetadata destSpawnerMetadata = launchedSpawnersMap.get(getId(transferRequest, false));
        if (destSpawnerMetadata != null) {
            selectedSpawnerMetadata.add(destSpawnerMetadata);
        }

        if (selectedSpawnerMetadata.isEmpty()) {
            Optional<AgentSpawner> sourceSpawner = SpawnerSelector.selectSpawner(
                    agentTransferRequest.getSourceStorage(),
                    agentTransferRequest.getSourceSecret());

            Optional<AgentSpawner> destSpawner = SpawnerSelector.selectSpawner(
                    agentTransferRequest.getDestinationStorage(),
                    agentTransferRequest.getDestinationSecret());

            if (sourceSpawner.isPresent()) {
                logger.info("Launching {} spawner in source side for transfer {}",
                        sourceSpawner.get().getClass().getName(), transferId);

                sourceSpawner.get().launch();
                LaunchedSpawnerMetadata lsm = new LaunchedSpawnerMetadata(sourceSpawner.get());
                lsm.transferInfos.put(agentTransferRequest.getRequestId(),
                        new TransferInfo(
                                transferId,
                                agentTransferRequest,
                                transferRequest,
                                consulKey));

                launchedSpawnersMap.put(getId(transferRequest, true), lsm);
                return true;

            } else if (destSpawner.isPresent()) {
                logger.info("Launching {} spawner in destination side for transfer {}",
                        destSpawner.get().getClass().getName(), transferId);

                destSpawner.get().launch();
                LaunchedSpawnerMetadata lsm = new LaunchedSpawnerMetadata(destSpawner.get());
                lsm.transferInfos.put(agentTransferRequest.getRequestId(),
                        new TransferInfo(
                                transferId,
                                agentTransferRequest,
                                transferRequest,
                                consulKey));

                launchedSpawnersMap.put(getId(transferRequest, false), lsm);
                return true;

            } else {
                return false;
            }
        } else {
            logger.info("Reusing already running optimized agents for transfer {}", transferId);

            // Todo select the spawner having least stransfers. Make this thread safe as some case, the spawner might be
            // initiating the termination
            selectedSpawnerMetadata.get(0).transferInfos.put(agentTransferRequest.getRequestId(),
                    new TransferInfo(
                            transferId,
                            agentTransferRequest,
                            transferRequest,
                            consulKey));
            return true;
        }
    }

    public boolean isAnAgentDeploying(String consulKey) {
        return this.launchedSpawnersMap.values().stream()
                .anyMatch(launchedSpawnerMetadata ->
                        launchedSpawnerMetadata.transferInfos.values().stream().anyMatch(
                                tinf-> tinf.consulKey.equals(consulKey)));
    }

    private String getId(TransferApiRequest transferRequest, boolean isSource) {
        if (isSource) {
            return transferRequest.getSourceStorageId() + transferRequest.getSourceSecretId();
        } else {
            return transferRequest.getDestinationStorageId() + transferRequest.getDestinationStorageId();
        }
    }
}
