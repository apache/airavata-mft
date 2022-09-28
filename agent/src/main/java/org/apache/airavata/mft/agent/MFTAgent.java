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
import com.google.protobuf.util.JsonFormat;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.MFTConsulClientException;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.agent.http.HttpServer;
import org.apache.airavata.mft.agent.http.HttpTransferRequestsStore;
import org.apache.airavata.mft.agent.rpc.RPCParser;
import org.apache.airavata.mft.api.service.CallbackEndpoint;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.core.FileResourceMetadata;
import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MFTAgent implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MFTAgent.class);

    @org.springframework.beans.factory.annotation.Value("${agent.id}")
    private String agentId;

    @org.springframework.beans.factory.annotation.Value("${agent.secret}")
    private String agentSecret;

    @org.springframework.beans.factory.annotation.Value("${agent.host}")
    private String agentHost;

    @org.springframework.beans.factory.annotation.Value("${agent.user}")
    private String agentUser;

    @org.springframework.beans.factory.annotation.Value("${agent.http.port}")
    private Integer agentHttpPort;

    @org.springframework.beans.factory.annotation.Value("${agent.https.enabled}")
    private boolean agentHttpsEnabled;

    @org.springframework.beans.factory.annotation.Value("${agent.supported.protocols}")
    private String supportedProtocols;

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

    @org.springframework.beans.factory.annotation.Value("${agent.concurrent.transfers}")
    private int concurrentTransfers;

    @org.springframework.beans.factory.annotation.Value("${agent.concurrent.chunked.threads}")
    private int concurrentChunkedThreads;

    @org.springframework.beans.factory.annotation.Value("${agent.chunk.size}")
    private int chunkedSize;

    @org.springframework.beans.factory.annotation.Value("${agent.chunk.streaming.enabled}")
    private boolean doChunkStream;

    private final Semaphore mainHold = new Semaphore(0);

    private KVCache transferMessageCache;
    private KVCache rpcMessageCache;

    private ConsulCache.Listener<String, Value> transferCacheListener;
    private ConsulCache.Listener<String, Value> rpcCacheListener;

    private final ScheduledExecutorService sessionRenewPool = Executors.newSingleThreadScheduledExecutor();
    private long sessionRenewSeconds = 4;
    private long sessionTTLSeconds = 10;
    private String session;
    private ExecutorService transferRequestExecutor;


    private TransportMediator mediator;


    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RPCParser rpcParser;

    @Autowired
    private MFTConsulClient mftConsulClient;

    @Autowired
    private HttpTransferRequestsStore transferRequestsStore;

    private final AtomicLong totalRunningTransfers = new AtomicLong(0);
    private final AtomicLong totalPendingTransfers = new AtomicLong(0);

    public void init() {
        transferMessageCache = KVCache.newCache(mftConsulClient.getKvClient(), MFTConsulClient.AGENTS_TRANSFER_REQUEST_MESSAGE_PATH + agentId);
        rpcMessageCache = KVCache.newCache(mftConsulClient.getKvClient(), MFTConsulClient.AGENTS_RPC_REQUEST_MESSAGE_PATH + agentId);
        mediator = new TransportMediator(tempDataDir,
                concurrentTransfers,
                concurrentChunkedThreads,
                chunkedSize, doChunkStream);
        transferRequestExecutor = Executors.newFixedThreadPool(concurrentTransfers);
    }

    private void acceptRPCRequests() {
        rpcCacheListener = newValues -> {
            newValues.values().forEach(value -> {
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    try {
                        SyncRPCRequest rpcRequest = mapper.readValue(v, SyncRPCRequest.class);
                        mftConsulClient.sendSyncRPCResponseFromAgent(rpcRequest.getReturnAddress(), rpcParser.processRPCRequest(rpcRequest));
                    } catch (Throwable e) {
                        logger.error("Error processing the RPC request {}", value.getKey(), e);
                    } finally {
                        mftConsulClient.getKvClient().deleteKey(value.getKey());
                    }
                });
            });
        };

        rpcMessageCache.addListener(rpcCacheListener);
        rpcMessageCache.start();
    }

    private void processTransfer(String transferId, String transferRequestJson) {
        logger.info("Received raw message: {}", transferRequestJson);
        TransferApiRequest request = null;
        try {
            TransferApiRequest.Builder builder = TransferApiRequest.newBuilder();
            JsonFormat.parser().merge(transferRequestJson, builder);
            request = builder.build();

            long running = totalRunningTransfers.incrementAndGet();
            long pending = totalPendingTransfers.decrementAndGet();
            logger.info("Received request {}. Total Running {}. Total Pending {}", transferId, running, pending);

            mftConsulClient.submitTransferStateToProcess(transferId, agentId, new TransferState()
                    .setState("STARTING")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setPublisher(agentId)
                    .setDescription("Starting the transfer"));

            Optional<MetadataCollector> srcMetadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(request.getSourceType());
            MetadataCollector srcMetadataCollector = srcMetadataCollectorOp.orElseThrow(() -> new Exception("Could not find a metadata collector for source"));
            srcMetadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);

            Optional<MetadataCollector> dstMetadataCollectorOp = MetadataCollectorResolver.resolveMetadataCollector(request.getDestinationType());
            MetadataCollector dstMetadataCollector = dstMetadataCollectorOp.orElseThrow(() -> new Exception("Could not find a metadata collector for destination"));
            dstMetadataCollector.init(resourceServiceHost, resourceServicePort, secretServiceHost, secretServicePort);

            FileResourceMetadata srcMetadata = srcMetadataCollector.getFileResourceMetadata(
                    request.getMftAuthorizationToken(),
                    request.getSourceResourceId(),
                    request.getSourceToken());


            ConnectorConfig srcCC = ConnectorConfig.ConnectorConfigBuilder.newBuilder()
                    .withAuthToken(request.getMftAuthorizationToken())
                    .withResourceServiceHost(resourceServiceHost)
                    .withResourceServicePort(resourceServicePort)
                    .withSecretServiceHost(secretServiceHost)
                    .withSecretServicePort(secretServicePort)
                    .withTransferId(transferId)
                    .withResourceId(request.getSourceResourceId())
                    .withCredentialToken(request.getSourceToken())
                    .withMetadata(srcMetadata).build();

            ConnectorConfig dstCC = ConnectorConfig.ConnectorConfigBuilder.newBuilder()
                    .withAuthToken(request.getMftAuthorizationToken())
                    .withResourceServiceHost(resourceServiceHost)
                    .withResourceServicePort(resourceServicePort)
                    .withSecretServiceHost(secretServiceHost)
                    .withSecretServicePort(secretServicePort)
                    .withTransferId(transferId)
                    .withResourceId(request.getDestinationResourceId())
                    .withCredentialToken(request.getDestinationToken())
                    .withMetadata(srcMetadata).build();

            mftConsulClient.submitTransferStateToProcess(transferId, agentId, new TransferState()
                    .setState("STARTED")
                    .setPercentage(0)
                    .setUpdateTimeMils(System.currentTimeMillis())
                    .setPublisher(agentId)
                    .setDescription("Started the transfer"));

            // Save transfer metadata in scheduled path to recover in case of an Agent failures. Recovery is done from controller
            mftConsulClient.getKvClient().putValue(MFTConsulClient.AGENTS_SCHEDULED_PATH + agentId + "/" + session + "/" + transferId, transferRequestJson);

            mediator.transferSingleThread(transferId, request, srcCC, dstCC,
                    (id, st) -> {
                        try {
                            mftConsulClient.submitTransferStateToProcess(id, agentId, st.setPublisher(agentId));

                        } catch (MFTConsulClientException e) {
                            logger.error("Failed while updating transfer state", e);
                        }
                    },
                    (id, transferSuccess) -> {
                        try {
                            // Delete scheduled key as the transfer completed / failed if it was placed in current session
                            mftConsulClient.getKvClient().deleteKey(MFTConsulClient.AGENTS_SCHEDULED_PATH + agentId + "/" + session + "/" + id);
                            long pendingAfter = totalRunningTransfers.decrementAndGet();
                            logger.info("Removed transfer {} from queue with transfer success = {}. Total running {}",
                                    id, transferSuccess, pendingAfter);
                        } catch (Exception e) {
                            logger.error("Failed while deleting scheduled path for transfer {}", id);
                        }
                    });


        } catch (Throwable e) {
            if (request != null) {
                try {
                    logger.error("Error in submitting transfer {}", transferId, e);

                    mftConsulClient.submitTransferStateToProcess(transferId, agentId, new TransferState()
                            .setState("FAILED")
                            .setPercentage(0)
                            .setUpdateTimeMils(System.currentTimeMillis())
                            .setPublisher(agentId)
                            .setDescription(ExceptionUtils.getStackTrace(e)));
                } catch (MFTConsulClientException ex) {
                    logger.warn(ex.getMessage());
                    // Ignore
                }
            } else {
                logger.error("Unknown error in processing message {}", transferRequestJson, e);
            }
        } finally {
            //logger.info("Deleting key " + consulEntryKey);
            //mftConsulClient.getKvClient().deleteKey(consulEntryKey); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
        }
    }
    private void acceptTransferRequests() {

        transferCacheListener = newValues -> {
            newValues.values().forEach(value -> {
                Optional<String> decodedValue = value.getValueAsString();
                String transferId = value.getKey().substring(value.getKey().lastIndexOf("/") + 1);
                decodedValue.ifPresent(v -> {
                    mftConsulClient.getKvClient().deleteKey(value.getKey());
                    long totalPending = totalPendingTransfers.incrementAndGet();
                    logger.info("Total pending transfers {}", totalPending);
                    transferRequestExecutor.submit(() -> processTransfer(transferId, v));
                });
            });
        };
        transferMessageCache.addListener(transferCacheListener);
        transferMessageCache.start();
    }

    private void handleCallbacks(List<CallbackEndpoint> callbackEndpoints, String transferId, TransferState transferState) {
        if (callbackEndpoints != null && !callbackEndpoints.isEmpty()) {
            for (CallbackEndpoint cbe : callbackEndpoints) {
                switch (cbe.getType()) {
                    case HTTP:
                        break;
                    case KAFKA:
                        break;
                }
            }
        }
    }

    private void acceptHTTPRequests() {
        logger.info("Starting the HTTP front end");

        new Thread(() -> {
            HttpServer httpServer = new HttpServer(agentHost, agentHttpPort, agentHttpsEnabled, transferRequestsStore);
            try {
                httpServer.run();
            } catch (Exception e) {
                logger.error("Http frontend server start failed", e);
            }
        }).start();
    }

    private boolean connectAgent() throws MFTConsulClientException {
        final ImmutableSession session = ImmutableSession.builder()
                .name(agentId)
                .behavior("delete")
                .ttl(sessionTTLSeconds + "s").build();

        final SessionCreatedResponse sessResp = mftConsulClient.getSessionClient().createSession(session);
        final String lockPath = MFTConsulClient.LIVE_AGENTS_PATH + agentId;

        boolean acquired = mftConsulClient.getKvClient().acquireLock(lockPath, sessResp.getId());

        if (acquired) {
            this.session = sessResp.getId();
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
                    .setSessionId(this.session)
                    .setSupportedProtocols(Arrays.asList(supportedProtocols.split(",")))
                    .setLocalStorages(new ArrayList<>()));
        }

        logger.info("Acquired lock " + acquired);
        return acquired;
    }

    public void disconnectAgent() {
        sessionRenewPool.shutdown();
        if (transferCacheListener != null) {
            transferMessageCache.removeListener(transferCacheListener);
        }

        if (rpcCacheListener != null) {
            rpcMessageCache.removeListener(rpcCacheListener);
        }
    }

    public void stop() {
        logger.info("Stopping Agent " + agentId);
        disconnectAgent();
        mainHold.release();
        transferRequestExecutor.shutdown();
    }

    public void start() throws Exception {
        init();
        boolean connected = false;
        int connectionRetries = 0;
        while (!connected) {
            connected = connectAgent();
            if (connected) {
                logger.info("Successfully connected to consul with session id {}", session);
            } else {
                logger.info("Retrying to connect to consul");
                Thread.sleep(5000);
                connectionRetries++;
                if (connectionRetries > 10) {
                    throw new Exception("Failed to connect to the cluster");
                }
            }
        }

        acceptTransferRequests();
        acceptRPCRequests();
        acceptHTTPRequests();
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
