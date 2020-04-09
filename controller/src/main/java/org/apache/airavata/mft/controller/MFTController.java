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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.MFTConsulClientException;
import org.apache.airavata.mft.admin.models.TransferCommand;
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.admin.models.TransferState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@SpringBootApplication()
@ComponentScan(basePackages = {"org.apache.airavata.mft"})
@EntityScan("org.apache.airavata.mft.api.db.entities")
public class MFTController implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MFTController.class);

    private final Semaphore mainHold = new Semaphore(0);
    private ObjectMapper mapper = new ObjectMapper();

    private KVCache messageCache;
    private KVCache stateCache;
    private KVCache liveAgentCache;
    private ScheduledExecutorService pendingMonitor;

    @Autowired
    private MFTConsulClient mftConsulClient;

    private ObjectMapper jsonMapper = new ObjectMapper();

    public void init() {
        logger.info("Initializing the Controller");
        messageCache = KVCache.newCache(mftConsulClient.getKvClient(), MFTConsulClient.CONTROLLER_TRANSFER_MESSAGE_PATH);
        stateCache = KVCache.newCache(mftConsulClient.getKvClient(), MFTConsulClient.CONTROLLER_STATE_MESSAGE_PATH);
        liveAgentCache = KVCache.newCache(mftConsulClient.getKvClient(), MFTConsulClient.LIVE_AGENTS_PATH);
        pendingMonitor = Executors.newSingleThreadScheduledExecutor();

        pendingMonitor.scheduleWithFixedDelay(this::processPending, 2000, 4000, TimeUnit.MILLISECONDS);
        logger.info("Controller initialized");
    }

    @PreDestroy
    public void destroy() {
        logger.info("Destroying the Controller");
        try {
            if (this.pendingMonitor != null) {
                this.pendingMonitor.shutdown();
            }
        } catch (Exception e) {
            logger.warn("Errored while shutting down the pending monitor", e);
        }
        logger.info("Controller destroyed");
    }

    /**
     * Accepts transfer requests coming from the API and put it into the pending queue
     */
    private void acceptRequests() {
        // Due to bug in consul https://github.com/hashicorp/consul/issues/571
        ConsulCache.Listener<String, Value> messageCacheListener = newValues -> newValues.forEach((key, value) -> {
            String transferId = key.substring(key.lastIndexOf("/") + 1);
            Optional<String> decodedValue = value.getValueAsString();
            decodedValue.ifPresent(v -> {
                logger.info("Received transfer request : {} with id {}", v, transferId);

                TransferRequest transferRequest;
                try {
                    transferRequest = jsonMapper.readValue(v, TransferRequest.class);
                } catch (IOException e) {
                    logger.error("Failed to parse the transfer request {}", v, e);
                    return;
                }

                try {
                    markAsPending(transferId, transferRequest);
                    logger.info("Marked transfer {} as pending", transferId);

                } catch (Exception e) {
                    logger.error("Failed to store transfer request {}", transferId, e);

                } finally {
                    logger.info("Deleting key " + value.getKey());
                    mftConsulClient.getKvClient().deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
                }
            });
        });
        messageCache.addListener(messageCacheListener);
        messageCache.start();
    }

    private void acceptStates() {
        ConsulCache.Listener<String, Value> stateCacheListener = newValues -> newValues.forEach((key, value) -> {
            try {
                if (value.getValueAsString().isPresent()) {
                    String valAsStr = value.getValueAsString().get();
                    logger.info("Received state Key {} val {}", key, valAsStr);

                    String parts[] = key.split("/");
                    if (parts.length != 3) {
                        logger.error("Invalid status key {}", key);
                    }

                    String transferId = parts[0];
                    String agentId = parts[1];
                    String time = parts[2];

                    TransferState transferState = mapper.readValue(valAsStr, TransferState.class);
                    mftConsulClient.saveTransferState(transferId, transferState);

                }
            } catch (Exception e) {
                logger.error("Error while processing the state message", e);
            } finally {
                logger.info("Deleting key " + value.getKey());
                mftConsulClient.getKvClient().deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
            }
        });
        stateCache.addListener(stateCacheListener);
        stateCache.start();
    }

    private void acceptLiveAgents() {
        ConsulCache.Listener<String, Value> liveAgentCacheListener = newValues -> newValues.forEach((agentId, value) -> {
            try {
                Optional<String> session = mftConsulClient.getKvClient().getSession(value.getKey());
                if (session.isPresent()) {
                    String sessionId = session.get();
                    logger.info("Agent connected in path {} agent id {} session {}", value.getKey(), agentId, sessionId);

                    List<Value> scheduledTransfers = mftConsulClient.getKvClient().getValues(MFTConsulClient.AGENTS_SCHEDULED_PATH + agentId);
                    for (Value v: scheduledTransfers) {
                        logger.info("Found scheduled key {}", v.getKey());

                        try {
                            // Key = AGENTS_SCHEDULED_PATH agent id / session id / transfer id
                            String[] parts = v.getKey().split("/");
                            // Make sure right amount of data is available
                            if (parts.length == MFTConsulClient.AGENTS_SCHEDULED_PATH.split("/").length + 3) {

                                String scheduledSession = parts[parts.length - 2];
                                String scheduledTransfer = parts[parts.length - 1];

                                logger.info("Scheduled session {} transfer {}", scheduledSession, scheduledTransfer);

                                if (!scheduledSession.equals(sessionId)) {
                                    logger.info("Old transfer session found. Re scheduling to agent {}", agentId);
                                    mftConsulClient.commandTransferToAgent(agentId,
                                                    mapper.readValue(v.getValueAsString().get(), TransferCommand.class));

                                    // Delete the key as it is already processed
                                    mftConsulClient.getKvClient().deleteKey(v.getKey());

                                } else {
                                    logger.info("Session {} is already active so skipping scheduled transfer", scheduledSession);
                                }

                            } else {
                                logger.warn("Invalid schedule key {}", v.getKey());
                            }

                        } catch (Exception e) {
                            logger.error("Failed to process schedule key {}", v.getKey());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Errored while processing live agent cache key {}", agentId, e);
            } finally {

            }
        });

        liveAgentCache.addListener(liveAgentCacheListener);
        liveAgentCache.start();
    }

    private void markAsProcessed(String transferId, TransferRequest transferRequest) throws JsonProcessingException {
        mftConsulClient.getKvClient().putValue(MFTConsulClient.TRANSFER_PROCESSED_PATH +transferId, jsonMapper.writeValueAsString(transferRequest));
    }

    private void markAsPending(String transferId, TransferRequest transferRequest) throws JsonProcessingException {
        mftConsulClient.getKvClient().putValue(MFTConsulClient.TRANSFER_PENDING_PATH +transferId, jsonMapper.writeValueAsString(transferRequest));
    }

    private TransferCommand convertRequestToCommand(String transferId, TransferRequest transferRequest) {
        TransferCommand transferCommand = new TransferCommand();
        transferCommand.setSourceId(transferRequest.getSourceId())
                .setSourceToken(transferRequest.getSourceToken())
                .setSourceType(transferRequest.getSourceType())
                .setSourceResourceBackend(transferRequest.getSourceResourceBackend())
                .setSourceCredentialBackend(transferRequest.getSourceCredentialBackend())
                .setDestinationId(transferRequest.getDestinationId())
                .setDestinationToken(transferRequest.getDestinationToken())
                .setDestinationType(transferRequest.getDestinationType())
                .setDestResourceBackend(transferRequest.getDestResourceBackend())
                .setDestCredentialBackend(transferRequest.getDestCredentialBackend())
                .setTransferId(transferId);
        return transferCommand;
    }

    private Optional<String> selectAgent(String transferId, TransferRequest transferRequest) throws MFTConsulClientException {

        List<String> liveAgentIds = mftConsulClient.getLiveAgentIds();
        if (liveAgentIds.isEmpty()) {
            logger.error("Live agents are not available. Skipping for now");
            return Optional.empty();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Processing transfer request {} with target agents {}", transferId, transferRequest.getTargetAgents());
            logger.debug("Printing live agents");
            liveAgentIds.forEach(a -> logger.debug("Agent {} is live", a));
        }

        String selectedAgent = null;
        if (transferRequest.getTargetAgents() != null && !transferRequest.getTargetAgents().isEmpty()) {
            Optional<String> possibleAgent = transferRequest.getTargetAgents().keySet()
                    .stream().filter(req -> liveAgentIds.stream().anyMatch(agent -> agent.equals(req))).findFirst();
            if (possibleAgent.isPresent()) {
                selectedAgent = possibleAgent.get();
            }
        } else if (!transferRequest.isAffinityTransfer()){
            selectedAgent = liveAgentIds.get(0);
        }

        if (selectedAgent == null) {
            logger.warn("Couldn't find an Agent that meet transfer requirements");
            return Optional.empty();
        }

        return Optional.of(selectedAgent);
    }

    /**
     * Fetch pending transfer requests and check for available agents. If an agent is found, forwards the transfer request
     * to that agent and mark transfer state as processed.
     */
    private void processPending() {
        List<Value> values = mftConsulClient.getKvClient().getValues(MFTConsulClient.TRANSFER_PENDING_PATH);
        logger.debug("Scanning pending transfers");

        values.forEach(value -> {

            if (value.getValueAsString().isPresent()) {
                logger.debug("Pending " + value.getKey() + " : " + value.getValueAsString().get());
                try {
                    TransferRequest transferRequest = jsonMapper.readValue(value.getValueAsString().get(), TransferRequest.class);
                    String transferId = value.getKey().substring(value.getKey().lastIndexOf("/") + 1);
                    Optional<String> agent = selectAgent(transferId, transferRequest);

                    if (agent.isPresent()) {
                        logger.info("Found agent {} to initiate the transfer {}", agent, transferId);
                        TransferCommand transferCommand = convertRequestToCommand(transferId, transferRequest);

                        mftConsulClient.commandTransferToAgent(agent.get(), transferCommand);
                        markAsProcessed(transferId, transferRequest);
                        mftConsulClient.getKvClient().deleteKey(value.getKey());
                        logger.info("Marked transfer {} as processed", transferId);
                    }
                } catch (Exception e) {
                    logger.error("Failed to process pending transfer in key {}", value.getKey(), e);
                }
            }
        });
    }

    @Override
    public void run(String... args) throws Exception {
        init();
        acceptRequests();
        acceptStates();
        acceptLiveAgents();
        mainHold.acquire();
    }

    public static void main(String args[]) {
        SpringApplication.run(MFTController.class);
    }
}
