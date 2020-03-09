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
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.MFTAdminException;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.TransferCommand;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.core.ConnectorResolver;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@PropertySource(value = {
        "classpath:application.properties",
        "file:${app.conf.dir}/application.properties"
}, ignoreResourceNotFound = true)
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

    @org.springframework.beans.factory.annotation.Value("${resource.service.host}")
    private String resourceServiceHost;

    @org.springframework.beans.factory.annotation.Value("${resource.service.port}")
    private int resourceServicePort;

    @org.springframework.beans.factory.annotation.Value("${secret.service.host}")
    private String secretServiceHost;

    @org.springframework.beans.factory.annotation.Value("${secret.service.port}")
    private int secretServicePort;

    private final Semaphore mainHold = new Semaphore(0);

    private KVCache messageCache;
    private ConsulCache.Listener<String, Value> cacheListener;

    private final ScheduledExecutorService sessionRenewPool = Executors.newSingleThreadScheduledExecutor();
    private long sessionRenewSeconds = 4;
    private long sessionTTLSeconds = 10;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MFTConsulClient mftConsulClient;

    public void init() {
        messageCache = KVCache.newCache(mftConsulClient.getKvClient(), "mft/agents/messages/" + agentId );
    }

    private void acceptRequests() {

        cacheListener = newValues -> {
            newValues.values().forEach(value -> {
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    logger.info("Received raw message: {}", v);
                    TransferCommand request = null;
                    try {
                        request = mapper.readValue(v, TransferCommand.class);
                        logger.info("Received request " + request.getTransferId());
                        mftConsulClient.submitTransferState(request.getTransferId(), new TransferState()
                            .setState("STARTING")
                            .setPercentage(0)
                            .setUpdateTimeMils(System.currentTimeMillis())
                            .setDescription("Starting the transfer"));

                        Optional<Connector> inConnectorOpt = ConnectorResolver.resolveConnector(request.getSourceType(), "IN");
                        Connector inConnector = inConnectorOpt.orElseThrow(() -> new Exception("Could not find an in connector for given input"));
                        inConnector.init(request.getSourceId(), request.getSourceToken(), resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);

                        Optional<Connector> outConnectorOpt = ConnectorResolver.resolveConnector(request.getDestinationType(), "OUT");
                        Connector outConnector = outConnectorOpt.orElseThrow(() -> new Exception("Could not find an out connector for given input"));
                        outConnector.init(request.getDestinationId(), request.getDestinationToken(), resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);

                        Optional<MetadataCollector> metadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(request.getSourceType());
                        MetadataCollector metadataCollector = metadataCollectorOp.orElseThrow(() -> new Exception("Could not find a metadata collector for input"));
                        metadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);

                        ResourceMetadata metadata = metadataCollector.getGetResourceMetadata(request.getSourceId(), request.getSourceToken());
                        logger.debug("File size " + metadata.getResourceSize());
                        mftConsulClient.submitTransferState(request.getTransferId(), new TransferState()
                            .setState("STARTED")
                            .setPercentage(0)
                            .setUpdateTimeMils(System.currentTimeMillis())
                            .setDescription("Started the transfer"));

                        String transferId = mediator.transfer(request.getTransferId(), inConnector, outConnector, metadata, (id, st) -> {
                            try {
                                mftConsulClient.submitTransferState(id, st);
                            } catch (MFTAdminException e) {
                                logger.error("Failed while updating transfer state", e);
                            }
                        });

                        logger.info("Submitted transfer " + transferId);

                    } catch (Exception e) {
                        if (request != null) {
                            try {
                                logger.error("Error in submitting transfer {}", request.getTransferId(), e);

                                mftConsulClient.submitTransferState(request.getTransferId(), new TransferState()
                                        .setState("FAILED")
                                        .setPercentage(0)
                                        .setUpdateTimeMils(System.currentTimeMillis())
                                        .setDescription(ExceptionUtils.getStackTrace(e)));
                            } catch (MFTAdminException ex) {
                                logger.warn(ex.getMessage());
                                // Ignore
                            }
                        } else {
                            logger.error("Unknown error in processing message {}", v, e);
                        }
                    } finally {
                        logger.info("Deleting key " + value.getKey());
                        mftConsulClient.getKvClient().deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
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

        final SessionCreatedResponse sessResp = mftConsulClient.getSessionClient().createSession(session);
        final String lockPath = "mft/agent/live/" + agentId;

        boolean acquired = mftConsulClient.getKvClient().acquireLock(lockPath, sessResp.getId());

        if (acquired) {
            sessionRenewPool.scheduleAtFixedRate(() -> {
                try {
                    mftConsulClient.getSessionClient().renewSession(sessResp.getId());
                } catch (ConsulException e) {
                    if (e.getCode() == 404) {
                        logger.error("Can not renew session as it is expired");
                        stop();
                    }
                    logger.warn("Errored while renewing the session", e);
                    try {
                        boolean status = mftConsulClient.getKvClient().acquireLock(lockPath, sessResp.getId());
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
                        boolean status = mftConsulClient.getKvClient().acquireLock(lockPath, sessResp.getId());
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

            this.mftConsulClient.registerAgent(new AgentInfo()
                    .setId(agentId)
                    .setHost(agentHost)
                    .setUser(agentUser)
                    .setSupportedProtocols(Arrays.asList(supportedProtocols.split(",")))
                    .setLocalStorages(new ArrayList<>()));
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
        boolean connected = false;
        int connectionRetries = 0;
        while (!connected) {
            connected = connectAgent();
            if (connected) {
                logger.info("Successfully connected to consul");
            } else {
                logger.info("Retrying to connect to consul");
                Thread.sleep(5000);
                connectionRetries++;
                if (connectionRetries > 10) {
                    throw new Exception("Failed to connect to the cluster");
                }
            }
        }
        acceptRequests();
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
