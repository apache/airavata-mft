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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class AgentTransferDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(AgentTransferDispatcher.class);
    private final Map<String, Pair<TransferApiRequest, AgentTransferRequest.Builder>> pendingTransferRequests = new ConcurrentHashMap<>();
    private final Map<String, String> pendingTransferConsulKeys = new ConcurrentHashMap<>();
    private final Map<String, Future<String>> pendingAgentSpawners = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> runningAgentCache = new ConcurrentHashMap<>();

    @Autowired
    private MFTConsulClient mftConsulClient;

    public void submitTransferToAgent(List<String> filteredAgents, String transferId,
                                      TransferApiRequest transferRequest,
                                      AgentTransferRequest.Builder agentTransferRequestTemplate, String consulKey)
            throws Exception {

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
        mftConsulClient.getKvClient().deleteKey(consulKey);
    }

    public void handleTransferRequest(String transferId,
                                      TransferApiRequest transferRequest,
                                      AgentTransferRequest.Builder agentTransferRequestTemplate,
                                      String consulKey) throws Exception{

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
                    Future<String> launchFuture = sourceSpawner.get().launch();
                    pendingAgentSpawners.put(getId(transferRequest, true), launchFuture);
                    pendingTransferRequests.put(getId(transferRequest, true),
                            Pair.of(transferRequest, agentTransferRequestTemplate));
                    pendingTransferConsulKeys.put(getId(transferRequest, true), consulKey);

                } else if (destSpawner.isPresent()) {
                    logger.info("Launching {} spawner in destination side for transfer {}",
                            destSpawner.get().getClass().getName(), transferId);

                    Future<String> launchFuture = destSpawner.get().launch();
                    pendingAgentSpawners.put(getId(transferRequest, false), launchFuture);
                    pendingTransferRequests.put(getId(transferRequest, false),
                            Pair.of(transferRequest, agentTransferRequestTemplate));
                    pendingTransferConsulKeys.put(getId(transferRequest, false), consulKey);

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

        logger.info("Marked transfer {} as processed", transferId);
    }

    private String getId(TransferApiRequest transferRequest, boolean isSource) {
        if (isSource) {
            return transferRequest.getSourceStorageId() + transferRequest.getSourceSecretId();
        } else {
            return transferRequest.getDestinationStorageId() + transferRequest.getDestinationStorageId();
        }
    }
}
