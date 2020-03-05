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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import org.apache.airavata.mft.admin.MFTAdmin;
import org.apache.airavata.mft.admin.MFTAdminException;
import org.apache.airavata.mft.admin.models.TransferCommand;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.api.db.entities.TransferEntity;
import org.apache.airavata.mft.api.db.entities.TransferStatusEntity;
import org.apache.airavata.mft.api.db.repositories.TransferRepository;
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.api.db.repositories.TransferStatusRepository;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

@PropertySource("classpath:application.properties")
@SpringBootApplication()
@ComponentScan(basePackages = {"org.apache.airavata.mft"})
@EnableJpaRepositories("org.apache.airavata.mft.api.db.repositories")
@EntityScan("org.apache.airavata.mft.api.db.entities")
public class MFTController implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MFTController.class);

    private final Semaphore mainHold = new Semaphore(0);

    private Consul client;
    private KeyValueClient kvClient;
    private KVCache messageCache;
    private KVCache stateCache;
    private ConsulCache.Listener<String, Value> messageCacheListener;
    private ConsulCache.Listener<String, Value> stateCacheListener;

    private MFTAdmin admin;

    private ObjectMapper jsonMapper = new ObjectMapper();
    private DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TransferStatusRepository statusRepository;

    public void init() {
        client = Consul.builder().build();
        kvClient = client.keyValueClient();
        messageCache = KVCache.newCache(kvClient, "mft/controller/messages");
        stateCache = KVCache.newCache(kvClient, "mft/transfer/state");
        admin = new MFTAdmin();
    }

    private void acceptRequests() {
        messageCacheListener = newValues -> {
            newValues.forEach((key, value) -> {
                String transferId = key.substring(key.lastIndexOf("/") + 1);
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    logger.info("Value is: {}", v);
                    try {
                        TransferRequest transferRequest = jsonMapper.readValue(v, TransferRequest.class);
                        TransferCommand transferCommand = saveAndCreateTransferCommand(transferId, transferRequest);
                        String selectedAgent = selectAgent(transferRequest);
                        admin.commandTransferToAgent(selectedAgent, transferCommand);
                    } catch (Exception e) {
                        logger.error("Failed to process the request", e);
                    } finally {
                        logger.info("Deleting key " + value.getKey());
                        kvClient.deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
                    }
                });
            });
        };
        messageCache.addListener(messageCacheListener);
        messageCache.start();
    }

    private void acceptStates() {
        stateCacheListener = newValues -> {
            newValues.forEach((key, value) -> {
                try {
                    if (value.getValueAsString().isPresent()) {
                        String asStr = value.getValueAsString().get();
                        logger.info("Received state {}", asStr);
                        TransferState transferState = jsonMapper.readValue(asStr, TransferState.class);
                        String transferId = key.substring(key.lastIndexOf("/") + 1);
                        Optional<TransferEntity> transferEntity = transferRepository.findById(transferId);
                        if (transferEntity.isPresent()) {
                            TransferStatusEntity ety = new TransferStatusEntity()
                                    .setPercentage(transferState.getPercentage())
                                    .setState(transferState.getState())
                                    .setUpdateTimeMils(transferState.getUpdateTimeMils())
                                    .setDescription(Optional.ofNullable(transferState.getDescription()).orElse(transferState.getState()))
                                    .setTransfer(transferEntity.get());
                            statusRepository.save(ety);
                            logger.info("Saved state for transfer {}", transferId);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error while processing the state message", e);
                } finally {
                    logger.info("Deleting key " + value.getKey());
                    kvClient.deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
                }
            });
        };
        stateCache.addListener(stateCacheListener);
        stateCache.start();
    }

    private TransferCommand saveAndCreateTransferCommand(String transferId, TransferRequest transferRequest) {
        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setTransferId(transferId);
        transferEntity.setSourceId(transferRequest.getSourceId())
                .setSourceToken(transferRequest.getSourceToken())
                .setSourceType(transferRequest.getSourceType())
                .setSourceResourceBackend(transferRequest.getSourceResourceBackend())
                .setSourceCredentialBackend(transferRequest.getSourceCredentialBackend())
                .setDestinationId(transferRequest.getDestinationId())
                .setDestinationToken(transferRequest.getDestinationToken())
                .setDestinationType(transferRequest.getDestinationType())
                .setDestResourceBackend(transferRequest.getDestResourceBackend())
                .setDestCredentialBackend(transferRequest.getDestCredentialBackend())
                .setAffinityTransfer(transferRequest.isAffinityTransfer());

        TransferEntity savedEntity = transferRepository.save(transferEntity);


        TransferCommand transferCommand = new TransferCommand();
        transferCommand.setSourceId(savedEntity.getSourceId())
                .setSourceToken(savedEntity.getSourceToken())
                .setSourceType(savedEntity.getSourceType())
                .setSourceResourceBackend(savedEntity.getSourceResourceBackend())
                .setSourceCredentialBackend(savedEntity.getSourceCredentialBackend())
                .setDestinationId(savedEntity.getDestinationId())
                .setDestinationToken(savedEntity.getDestinationToken())
                .setDestinationType(savedEntity.getDestinationType())
                .setDestResourceBackend(savedEntity.getDestResourceBackend())
                .setDestCredentialBackend(savedEntity.getDestCredentialBackend())
                .setTransferId(savedEntity.getTransferId());

        return transferCommand;
    }

    private String selectAgent(TransferRequest transferRequest) throws ControllerException, MFTAdminException {

        List<String> liveAgentIds = admin.getLiveAgentIds();
        if (liveAgentIds.isEmpty()) {
            logger.error("Live agents are not available. Skipping for now");
            throw new ControllerException("Live agents are not available. Skipping for now");
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
            logger.error("Couldn't find an Agent that meet transfer requirements");
            throw new ControllerException("Couldn't find an Agent that meet transfer requirements");
        }

        return selectedAgent;
    }

    @Override
    public void run(String... args) throws Exception {
        init();
        acceptRequests();
        acceptStates();
        mainHold.acquire();
    }

    public static void main(String args[]) {
        SpringApplication.run(MFTController.class);
    }
}
