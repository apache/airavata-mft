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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import org.apache.airavata.mft.admin.MFTAdmin;
import org.apache.airavata.mft.admin.MFTAdminException;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.transport.local.LocalMetadataCollector;
import org.apache.airavata.mft.transport.local.LocalReceiver;
import org.apache.airavata.mft.transport.local.LocalSender;
import org.apache.airavata.mft.transport.scp.SCPMetadataCollector;
import org.apache.airavata.mft.transport.scp.SCPReceiver;
import org.apache.airavata.mft.transport.scp.SCPSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@PropertySource("classpath:application.properties")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MFTAgent implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MFTAgent.class);

    private final TransportMediator mediator = new TransportMediator();

    @org.springframework.beans.factory.annotation.Value("${agent.id}")
    private String agentId;
    @org.springframework.beans.factory.annotation.Value("${agent.host}")
    private String agentHost;
    @org.springframework.beans.factory.annotation.Value("${agent.user}")
    private String agentUser;
    @org.springframework.beans.factory.annotation.Value("${agent.supported.protocols}")
    private String supportedProtocols;

    private final Semaphore mainHold = new Semaphore(0);

    private Consul client;
    private KeyValueClient kvClient;
    private KVCache messageCache;
    private ConsulCache.Listener<String, Value> cacheListener;

    private final ScheduledExecutorService sessionRenewPool = Executors.newSingleThreadScheduledExecutor();
    private long sessionRenewSeconds = 4;
    private long sessionTTLSeconds = 10;

    private MFTAdmin admin;

    public void init() {
        client = Consul.builder().build();
        kvClient = client.keyValueClient();
        messageCache = KVCache.newCache(kvClient, "mft/agents/messages/" + agentId );
        admin = new MFTAdmin();
    }

    private void acceptRequests() {

        cacheListener = newValues -> {
            newValues.values().forEach(value -> {
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    System.out.println(String.format("Value is: %s", v));
                    ObjectMapper mapper = new ObjectMapper();
                    TransferRequest request = null;
                    try {
                        request = mapper.readValue(v, TransferRequest.class);
                        logger.info("Received request " + request.getTransferId());
                        admin.updateTransferState(request.getTransferId(), new TransferState().setState("STARTING")
                                .setPercentage(0).setUpdateTimeMils(System.currentTimeMillis()));

                        Connector inConnector = MFTAgent.this.resolveConnector(request.getSourceType(), "IN");
                        inConnector.init(request.getSourceId(), request.getSourceToken());
                        Connector outConnector = MFTAgent.this.resolveConnector(request.getDestinationType(), "OUT");
                        outConnector.init(request.getDestinationId(), request.getDestinationToken());

                        MetadataCollector metadataCollector = MFTAgent.this.resolveMetadataCollector(request.getSourceType());
                        ResourceMetadata metadata = metadataCollector.getGetResourceMetadata(request.getSourceId(), request.getSourceToken());
                        logger.debug("File size " + metadata.getResourceSize());
                        admin.updateTransferState(request.getTransferId(), new TransferState().setState("STARTED")
                                .setPercentage(0).setUpdateTimeMils(System.currentTimeMillis()));

                        String transferId = mediator.transfer(request.getTransferId(), inConnector, outConnector, metadata, (id, st) -> {
                            try {
                                admin.updateTransferState(id, st);
                            } catch (MFTAdminException e) {
                                logger.error("Failed while updating transfer state", e);
                            }
                        });

                        logger.info("Submitted transfer " + transferId);

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (request != null) {
                            try {
                                admin.updateTransferState(request.getTransferId(), new TransferState().setState("FAILED")
                                        .setPercentage(0).setUpdateTimeMils(System.currentTimeMillis()));
                            } catch (MFTAdminException ex) {
                                ex.printStackTrace();
                                logger.warn(ex.getMessage());
                                // Ignore
                            }
                        }
                    } finally {
                        logger.info("Deleting key " + value.getKey());
                        kvClient.deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
                    }
                });

            });
        };
        messageCache.addListener(cacheListener);
        messageCache.start();
    }

    private boolean connectAgent() throws MFTAdminException {
        final ImmutableSession session = ImmutableSession.builder()
                .name(agentId)
                .behavior("delete")
                .ttl(sessionTTLSeconds + "s").build();

        final SessionCreatedResponse sessResp = client.sessionClient().createSession(session);
        final String lockPath = "mft/agent/live/" + agentId;

        boolean acquired = kvClient.acquireLock(lockPath, sessResp.getId());

        if (acquired) {
            sessionRenewPool.scheduleAtFixedRate(() -> {
                try {
                    client.sessionClient().renewSession(sessResp.getId());
                } catch (ConsulException e) {
                    if (e.getCode() == 404) {
                        logger.error("Can not renew session as it is expired");
                        stop();
                    }
                    logger.warn("Errored while renewing the session", e);
                    try {
                        boolean status = kvClient.acquireLock(lockPath, sessResp.getId());
                        if (!status) {
                            logger.error("Can not renew session as it is expired");
                            stop();
                        }
                    } catch (Exception ex) {
                        logger.error("Can not renew session as it is expired");
                        stop();
                    }
                } catch (Exception e) {
                    try {
                        boolean status = kvClient.acquireLock(lockPath, sessResp.getId());
                        if (!status) {
                            logger.error("Can not renew session as it is expired");
                            stop();
                        }
                    } catch (Exception ex) {
                        logger.error("Can not renew session as it is expired");
                        stop();
                    }
                }
            }, sessionRenewSeconds, sessionRenewSeconds, TimeUnit.SECONDS);

            this.admin.registerAgent(new AgentInfo()
                    .setId(agentId)
                    .setHost(agentHost)
                    .setUser(agentUser)
                    .setSupportedProtocols(Arrays.asList(supportedProtocols.split(","))));
        }

        logger.info("Acquired lock " + acquired);
        return acquired;
    }

    public void disconnectAgent() {
        sessionRenewPool.shutdown();
        if (cacheListener != null) {
            messageCache.removeListener(cacheListener);
        }
    }

    public void stop() {
        logger.info("Stopping Agent " + agentId);
        disconnectAgent();
        mainHold.release();
    }

    public void start() throws Exception {
        init();
        boolean connected = connectAgent();
        if (!connected) {
            throw new Exception("Failed to connect to the cluster");
        }
        acceptRequests();
    }

    // TODO load from reflection to avoid dependencies
    private Connector resolveConnector(String type, String direction) {
        switch (type) {
            case "SCP":
                switch (direction) {
                    case "IN":
                        return new SCPReceiver();
                    case "OUT":
                        return new SCPSender();
                }
                break;
            case "LOCAL":
                switch (direction) {
                    case "IN":
                        return new LocalReceiver();
                    case "OUT":
                        return new LocalSender();
                }
        }
        return null;
    }

    // TODO load from reflection to avoid dependencies
    private MetadataCollector resolveMetadataCollector(String type) {
        switch (type) {
            case "SCP":
                return new SCPMetadataCollector();
            case "LOCAL":
                return new LocalMetadataCollector();
        }
        return null;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Agent " + agentId);
        start();
        mainHold.acquire();
        logger.info("Agent exited");
    }

    public static void main(String args[]) throws Exception {
        SpringApplication.run(MFTAgent.class);
    }
}
