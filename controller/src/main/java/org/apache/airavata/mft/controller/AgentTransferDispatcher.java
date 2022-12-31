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

package org.apache.airavata.mft.controller;

import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.agent.stub.AgentTransferRequest;
import org.apache.airavata.mft.api.service.EndpointPaths;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.controller.spawner.CloudAgentSpawner;
import org.apache.airavata.mft.controller.spawner.SpawnerSelector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AgentTransferDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(AgentTransferDispatcher.class);

    //getId(transferRequest):Pair<TransferApiRequest, AgentTransferRequest.Builder>

    private final Map<String, Pair<TransferApiRequest, AgentTransferRequest.Builder>> pendingTransferRequests = new ConcurrentHashMap<>();
    private final Map<String, String> pendingTransferIds = new ConcurrentHashMap<>();
    //getId(transferRequest):consulKey

    private final Map<String, String> pendingTransferConsulKeys = new ConcurrentHashMap<>();

    //getId(transferRequest):CloudAgentSpawner
    private final Map<String, CloudAgentSpawner> pendingAgentSpawners = new ConcurrentHashMap<>();

    // getId(transferRequest):Set(TransferId)
    private final Map<String, Set<String>> runningAgentCache = new ConcurrentHashMap<>();

    // AgentID:Spawner - Use this to keep track of agent spawners. This is required to terminate agent
    private final Map<String, CloudAgentSpawner> agentSpawners = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Temporarily store consul key until the optimizer spins up Agents. This will block the same pending transfer
    // being handled twice
    private final Set<String> optimizingConsulKeys = new ConcurrentSkipListSet<>();

    @Autowired
    private MFTConsulClient mftConsulClient;

    public void init() {
        scheduler.scheduleWithFixedDelay(() -> {
            pendingAgentSpawners.forEach((key, spawner) -> {
                if (spawner.getLaunchState().isDone()) {
                    String transferId = pendingTransferIds.get(key);
                    Pair<TransferApiRequest, AgentTransferRequest.Builder> transferRequests = pendingTransferRequests.get(key);
                    String consulKey = pendingTransferConsulKeys.get(key);

                    try {
                        String agentId = spawner.getLaunchState().get();
                        List<String> liveAgentIds = mftConsulClient.getLiveAgentIds();
                        if (liveAgentIds.stream().noneMatch(id -> id.equals(agentId))) {
                            throw new Exception("Agent was not registered even though the agent is up");
                        }

                        submitTransferToAgent(Collections.singletonList(agentId), transferId,
                                transferRequests.getLeft(), transferRequests.getRight(), consulKey);

                        // Use this to terminate agent in future
                        agentSpawners.put(agentId, spawner);

                    } catch (Exception e) {
                        logger.error("Failed to launch agent for key {}", key, e);
                        try {
                            mftConsulClient.saveTransferState(transferId, new TransferState()
                                    .setUpdateTimeMils(System.currentTimeMillis())
                                    .setState("FAILED").setPercentage(0)
                                    .setPublisher("controller")
                                    .setDescription("Failed to launch the agent. " + ExceptionUtils.getRootCauseMessage(e)));
                        } catch (Exception e2) {
                            logger.error("Failed to submit transfer fail error for transfer id {}", transferId, e2);
                        }

                        logger.info("Removing consul key {}", consulKey);
                        mftConsulClient.getKvClient().deleteKey(consulKey);
                        logger.info("Terminating the spawner");
                        spawner.terminate();

                    } finally {
                        pendingTransferIds.remove(key);
                        pendingTransferRequests.remove(key);
                        pendingAgentSpawners.remove(key);
                        pendingTransferConsulKeys.remove(key);
                        optimizingConsulKeys.remove(consulKey);
                    }
                }
            });
        }, 3, 5, TimeUnit.SECONDS);
    }


    public void submitTransferToAgent(List<String> filteredAgents, String transferId,
                                      TransferApiRequest transferRequest,
                                      AgentTransferRequest.Builder agentTransferRequestTemplate, String consulKey)
            throws Exception {

        try {
            if (filteredAgents.isEmpty()) {
                mftConsulClient.saveTransferState(transferId, new TransferState()
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setState("FAILED").setPercentage(0)
                        .setPublisher("controller")
                        .setDescription("No qualifying agent was found to orchestrate the transfer"));
                return;
            }

            mftConsulClient.saveTransferState(transferId, new TransferState()
                    .setState("STARTING")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setPublisher("controller")
                    .setDescription("Initializing the transfer"));

            AgentTransferRequest.Builder agentTransferRequest = agentTransferRequestTemplate.clone();

            agentTransferRequest.setRequestId(UUID.randomUUID().toString());
            for (EndpointPaths ep : transferRequest.getEndpointPathsList()) {
                agentTransferRequest.addEndpointPaths(org.apache.airavata.mft.agent.stub.EndpointPaths.newBuilder()
                        .setSourcePath(ep.getSourcePath())
                        .setDestinationPath(ep.getDestinationPath()).buildPartial());
            }

            // TODO use a better way to select the right agent
            mftConsulClient.commandTransferToAgent(filteredAgents.get(0), transferId, agentTransferRequest.build());
            mftConsulClient.markTransferAsProcessed(transferId, transferRequest);
            logger.info("Marked transfer {} as processed", transferId);
        } finally {
            mftConsulClient.getKvClient().deleteKey(consulKey);
        }
    }

    public void handleTransferRequest(String transferId,
                                      TransferApiRequest transferRequest,
                                      AgentTransferRequest.Builder agentTransferRequestTemplate,
                                      String consulKey) throws Exception{

        if (optimizingConsulKeys.contains(consulKey)) {
            logger.info("Ignoring handling transfer id {} as it is already in optimizing stage", transferId);
            return;
        }

        logger.info("Handling transfer id {} with consul key {}", transferId, consulKey);
        List<String> liveAgentIds = mftConsulClient.getLiveAgentIds();

        Map<String, Integer> targetAgentsMap = transferRequest.getTargetAgentsMap();
        List<String> userProvidedAgents = liveAgentIds.stream().filter(targetAgentsMap::containsKey).collect(Collectors.toList());
        List<String> optimizedAgents = new ArrayList<>();

        if (transferRequest.getOptimizeTransferPath()) {

            Set<String> sourceAgents = runningAgentCache.get(getId(transferRequest, true));
            if (sourceAgents != null) {
                optimizedAgents.addAll(liveAgentIds.stream().filter(sourceAgents::contains).collect(Collectors.toList()));
            }

            Set<String> destAgents = runningAgentCache.get(getId(transferRequest, false));
            if (destAgents != null) {
                optimizedAgents.addAll(liveAgentIds.stream().filter(destAgents::contains).collect(Collectors.toList()));
            }

            if (optimizedAgents.isEmpty()) {
                Optional<CloudAgentSpawner> sourceSpawner = SpawnerSelector.selectSpawner(
                        agentTransferRequestTemplate.getSourceStorage(),
                        agentTransferRequestTemplate.getSourceSecret());

                Optional<CloudAgentSpawner> destSpawner = SpawnerSelector.selectSpawner(
                        agentTransferRequestTemplate.getDestinationStorage(),
                        agentTransferRequestTemplate.getDestinationSecret());

                if (sourceSpawner.isPresent()) {
                    logger.info("Launching {} spawner in source side for transfer {}",
                            sourceSpawner.get().getClass().getName(), transferId);

                    sourceSpawner.get().launch();
                    pendingAgentSpawners.put(getId(transferRequest, true), sourceSpawner.get());
                    pendingTransferRequests.put(getId(transferRequest, true),
                            Pair.of(transferRequest, agentTransferRequestTemplate));
                    pendingTransferIds.put(getId(transferRequest, true), transferId);
                    pendingTransferConsulKeys.put(getId(transferRequest, true), consulKey);
                    optimizingConsulKeys.add(consulKey);
                    return;
                } else if (destSpawner.isPresent()) {
                    logger.info("Launching {} spawner in destination side for transfer {}",
                            destSpawner.get().getClass().getName(), transferId);

                    destSpawner.get().launch();
                    pendingAgentSpawners.put(getId(transferRequest, false), destSpawner.get());
                    pendingTransferRequests.put(getId(transferRequest, false),
                            Pair.of(transferRequest, agentTransferRequestTemplate));
                    pendingTransferIds.put(getId(transferRequest, false), transferId);
                    pendingTransferConsulKeys.put(getId(transferRequest, false), consulKey);
                    optimizingConsulKeys.add(consulKey);
                    return;
                } else {
                    logger.warn("No optimizing path is available. Moving user provided agents");
                    submitTransferToAgent(userProvidedAgents, transferId,
                            transferRequest,
                            agentTransferRequestTemplate,
                            consulKey);
                }
            } else {
                logger.info("Using optimized agents for transfer {}", transferId);
                submitTransferToAgent(optimizedAgents, transferId,
                        transferRequest,
                        agentTransferRequestTemplate,
                        consulKey);
            }
        }

        if (userProvidedAgents.isEmpty()) {
            if (liveAgentIds.isEmpty()) {
                logger.warn("No live agent available to perform the transfer.");
                return;
            }
            logger.info("No agent selection criteria was provided. Going with the local agent");
            // TODO select the local agent
            submitTransferToAgent(liveAgentIds, transferId,
                    transferRequest,
                    agentTransferRequestTemplate,
                    consulKey);

        } else {
            submitTransferToAgent(userProvidedAgents, transferId,
                    transferRequest,
                    agentTransferRequestTemplate,
                    consulKey);
        }
    }

    private String getId(TransferApiRequest transferRequest, boolean isSource) {
        if (isSource) {
            return transferRequest.getSourceStorageId() + transferRequest.getSourceSecretId();
        } else {
            return transferRequest.getDestinationStorageId() + transferRequest.getDestinationStorageId();
        }
    }
}
