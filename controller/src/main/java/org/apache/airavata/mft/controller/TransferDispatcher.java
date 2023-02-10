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
import org.apache.airavata.mft.admin.MFTConsulClientException;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.agent.stub.AgentTransferRequest;
import org.apache.airavata.mft.api.service.EndpointPaths;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.controller.spawner.AgentOrchestrator;
import org.apache.airavata.mft.controller.spawner.AgentSpawner;
import org.apache.airavata.mft.controller.spawner.SpawnerSelector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TransferDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(TransferDispatcher.class);

    private AgentOrchestrator agentOrchestrator;

    @Autowired
    private MFTConsulClient mftConsulClient;

    public void init() {
        agentOrchestrator = new AgentOrchestrator(this);
        agentOrchestrator.init();
    }

    public void submitTransferToAgent(List<String> filteredAgents, String transferId,
                                      TransferApiRequest transferRequest,
                                      AgentTransferRequest agentTransferRequest, String consulKey) {

        try {
            if (filteredAgents.isEmpty()) {
                mftConsulClient.saveTransferState(transferId, null, new TransferState()
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setState("FAILED").setPercentage(0)
                        .setPublisher("controller")
                        .setDescription("No qualifying agent was found to orchestrate the transfer"));
                return;
            }

            mftConsulClient.saveTransferState(transferId,null, new TransferState()
                    .setState("STARTING")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setPublisher("controller")
                    .setDescription("Initializing the transfer"));

            // TODO use a better way to select the right agent
            mftConsulClient.commandTransferToAgent(selectTargetAgent(filteredAgents), transferId, agentTransferRequest);
            mftConsulClient.markTransferAsProcessed(transferId, transferRequest);
            logger.info("Marked transfer {} as processed", transferId);

        } catch (Exception e) {

            logger.error("Failed to submit the transfer {} to agent", transferId, e);

            try {
                mftConsulClient.saveTransferState(transferId, null, new TransferState()
                        .setUpdateTimeMils(System.currentTimeMillis())
                        .setState("FAILED").setPercentage(0)
                        .setPublisher("controller")
                        .setDescription("Failed to submit the transfer to agent. Error: " + ExceptionUtils.getRootCauseMessage(e)));
            } catch (Exception e2) {
                // Ignore
                logger.warn("Failed to update the failed transfer state for transfer id {}", transferId, e);
            }
        } finally {
            mftConsulClient.getKvClient().deleteKey(consulKey);
        }
    }


    private String selectTargetAgent(List<String> liveAgentIds) throws MFTConsulClientException {
        String selectedAgent = null;
        List<Optional<AgentInfo>> agentInfos = liveAgentIds.stream().map(
                id -> mftConsulClient.getAgentInfo(id)).collect(Collectors.toList());
        long transferCount = -1;
        List<String> candidates = new ArrayList<>();

        for (Optional<AgentInfo> agentInfo : agentInfos) {
            if (agentInfo.isPresent()) {
                int agentActiveTransfers = mftConsulClient.getEndpointHookCountForAgent(agentInfo.get().getId());
                long pendingTransferCount = mftConsulClient.getAgentPendingTransferCount(agentInfo.get().getId());
                long totalTransferCount = agentActiveTransfers + pendingTransferCount;
                logger.info("Agent {} has transfers assigned {}", agentInfo.get().getId(), totalTransferCount);
                if (transferCount == -1) {
                    transferCount = totalTransferCount;
                    candidates.add(agentInfo.get().getId());
                } else if (transferCount == totalTransferCount) {
                    candidates.add(agentInfo.get().getId());
                } else if (transferCount > totalTransferCount) {
                    candidates = new ArrayList<>();
                    transferCount = totalTransferCount;
                    candidates.add(agentInfo.get().getId());
                }
            }
        }

        if (candidates.size() > 0) {
            Random rand = new Random();
            selectedAgent = candidates.get(rand.nextInt(candidates.size()));
            logger.info("Selecting agent {}", selectedAgent);
        }

        return selectedAgent;
    }

    public void handleTransferRequest(String transferId,
                                      TransferApiRequest transferRequest,
                                      AgentTransferRequest.Builder agentTransferRequestTemplate,
                                      String consulKey) throws Exception{

        if (this.agentOrchestrator.isAnAgentDeploying(consulKey)) {
            logger.info("Ignoring handling transfer id {} as it is already in optimizing stage", transferId);
            return;
        }

        logger.info("Handling transfer id {} with consul key {}", transferId, consulKey);
        List<String> liveAgentIds = mftConsulClient.getLiveAgentIds();

        Map<String, Integer> targetAgentsMap = transferRequest.getTargetAgentsMap();
        List<String> userProvidedAgents = liveAgentIds.stream().filter(targetAgentsMap::containsKey).collect(Collectors.toList());

        AgentTransferRequest.Builder agentTransferRequestBuilder = agentTransferRequestTemplate.clone();

        agentTransferRequestBuilder.setRequestId(UUID.randomUUID().toString());
        for (EndpointPaths ep : transferRequest.getEndpointPathsList()) {
            agentTransferRequestBuilder.addEndpointPaths(org.apache.airavata.mft.agent.stub.EndpointPaths.newBuilder()
                    .setSourcePath(ep.getSourcePath())
                    .setDestinationPath(ep.getDestinationPath()).buildPartial());
        }

        AgentTransferRequest agentTransferRequest = agentTransferRequestBuilder.build();

        if (transferRequest.getOptimizeTransferPath()) {
            boolean agentLaunching = agentOrchestrator.tryLaunchingAgent(
                    transferId, transferRequest,
                    agentTransferRequest,
                    consulKey);

            if (!agentLaunching) {
                logger.warn("No optimizing path is available. Moving user provided agents");
                submitTransferToAgent(userProvidedAgents, transferId,
                        transferRequest,
                        agentTransferRequest,
                        consulKey);
            }

        } else if (userProvidedAgents.isEmpty()) {
            if (liveAgentIds.isEmpty()) {
                logger.warn("No live agent available to perform the transfer.");
                return;
            }
            logger.info("No agent selection criteria was provided. Going with the local agent");
            // TODO select the local agent
            submitTransferToAgent(liveAgentIds, transferId,
                    transferRequest,
                    agentTransferRequest,
                    consulKey);

        } else {
            submitTransferToAgent(userProvidedAgents, transferId,
                    transferRequest,
                    agentTransferRequest,
                    consulKey);
        }
    }

    public MFTConsulClient getMftConsulClient() {
        return mftConsulClient;
    }
}
