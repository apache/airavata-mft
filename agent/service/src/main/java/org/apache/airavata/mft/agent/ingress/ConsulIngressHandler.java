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

package org.apache.airavata.mft.agent.ingress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.model.session.ImmutableSession;
import com.orbitz.consul.model.session.SessionCreatedResponse;
import com.orbitz.consul.option.PutOptions;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.MFTConsulClientException;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCResponse;
import org.apache.airavata.mft.agent.AgentUtil;
import org.apache.airavata.mft.agent.TransferOrchestrator;
import org.apache.airavata.mft.agent.rpc.RPCParser;
import org.apache.airavata.mft.agent.stub.AgentTransferRequest;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ConsulIngressHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsulIngressHandler.class);

    private KVCache transferMessageCache;
    private KVCache rpcMessageCache;

    private ConsulCache.Listener<String, Value> transferCacheListener;
    private ConsulCache.Listener<String, Value> rpcCacheListener;
    @Autowired
    private MFTConsulClient mftConsulClient;
    @Autowired
    private RPCParser rpcParser;

    private String session;

    private final ScheduledExecutorService sessionRenewPool = Executors.newSingleThreadScheduledExecutor();

    private long sessionRenewSeconds = 4;
    private long sessionTTLSeconds = 10;

    private ObjectMapper mapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${agent.id}")
    private String agentId;

    @org.springframework.beans.factory.annotation.Value("${agent.secret}")
    private String agentSecret;

    @org.springframework.beans.factory.annotation.Value("${agent.host}")
    private String agentHost;

    @org.springframework.beans.factory.annotation.Value("${agent.user}")
    private String agentUser;

    @org.springframework.beans.factory.annotation.Value("${agent.supported.protocols}")
    private String supportedProtocols;

    @Autowired
    private TransferOrchestrator transferOrchestrator;

    private void acceptTransferRequests() {

        transferCacheListener = newValues -> {
            newValues.values().forEach(value -> {
                Optional<byte[]> decodedValue = value.getValueAsBytes();

                String[] partsOfKey = value.getKey().split("/");
                String agentTransferRequestId = partsOfKey[partsOfKey.length  - 1];
                String transferId = partsOfKey[partsOfKey.length  - 2];

                decodedValue.ifPresent(reqBytes -> {
                    mftConsulClient.getKvClient().deleteKey(value.getKey());
                    AgentTransferRequest.Builder builder = null;
                    try {
                        builder = AgentTransferRequest.newBuilder().mergeFrom(reqBytes);
                    } catch (InvalidProtocolBufferException e) {
                        logger.error("Failed to merge transfer request {} for transfer {} from bytes", agentTransferRequestId, transferId, e);
                        return;
                    }

                    AgentTransferRequest request = builder.build();
                    transferOrchestrator.submitTransferToProcess(transferId, request,
                            AgentUtil.throwingBiConsumerWrapper((endPointPath, st) -> {
                                mftConsulClient.submitFileTransferStateToProcess(transferId, request.getRequestId(), endPointPath,  agentId, st.setPublisher(agentId));
                            }),
                            AgentUtil.throwingBiConsumerWrapper((endpointPath, create) -> {
                                if (create) {
                                    mftConsulClient.createEndpointHookForAgent(agentId, session, transferId, agentTransferRequestId, endpointPath);
                                } else {
                                   mftConsulClient.deleteEndpointHookForAgent(agentId, session, transferId, agentTransferRequestId, endpointPath);
                                }
                            }));
                });
            });
        };
        transferMessageCache.addListener(transferCacheListener);
        transferMessageCache.start();
    }

    private void acceptRPCRequests() {
        rpcCacheListener = newValues -> {
            newValues.values().forEach(value -> {
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    try {
                        SyncRPCRequest rpcRequest = mapper.readValue(v, SyncRPCRequest.class);
                        SyncRPCResponse syncRPCResponse = rpcParser.processRPCRequest(rpcRequest);
                        mftConsulClient.sendSyncRPCResponseFromAgent(rpcRequest.getReturnAddress(), syncRPCResponse);
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
                        destroy();
                    }
                    logger.warn("Errored while renewing the session", e);
                    try {
                        boolean status = mftConsulClient.getKvClient().acquireLock(lockPath, sessResp.getId());
                        if (!status) {
                            logger.error("Can not renew session as it is expired");
                            destroy();
                        }
                    } catch (Exception ex) {
                        logger.error("Can not renew session as it is expired");
                        destroy();
                    }
                } catch (Exception e) {
                    try {
                        boolean status = mftConsulClient.getKvClient().acquireLock(lockPath, sessResp.getId());
                        if (!status) {
                            logger.error("Can not renew session as it is expired");
                            destroy();
                        }
                    } catch (Exception ex) {
                        logger.error("Can not renew session as it is expired");
                        destroy();
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

    @PostConstruct
    public void init() throws Exception {
        transferMessageCache = KVCache.newCache(mftConsulClient.getKvClient(), MFTConsulClient.AGENTS_TRANSFER_REQUEST_MESSAGE_PATH + agentId, 9);
        rpcMessageCache = KVCache.newCache(mftConsulClient.getKvClient(), MFTConsulClient.AGENTS_RPC_REQUEST_MESSAGE_PATH + agentId, 9);

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
        logger.info("Consul ingress handler initialized");

    }

    @PreDestroy
    public void destroy() {
        if (!sessionRenewPool.isShutdown())
            sessionRenewPool.shutdown();
        if (transferCacheListener != null) {
            transferMessageCache.removeListener(transferCacheListener);
        }

        if (rpcCacheListener != null) {
            rpcMessageCache.removeListener(rpcCacheListener);
        }
        logger.info("Consul ingress handler turned off");
    }

}
