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
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.core.ResourceMetadata;
import org.apache.airavata.mft.core.TransportMediator;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.transport.local.LocalMetadataCollector;
import org.apache.airavata.mft.transport.local.LocalReceiver;
import org.apache.airavata.mft.transport.local.LocalSender;
import org.apache.airavata.mft.transport.scp.SCPMetadataCollector;
import org.apache.airavata.mft.transport.scp.SCPReceiver;
import org.apache.airavata.mft.transport.scp.SCPSender;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MFTAgent {

    private final TransportMediator mediator = new TransportMediator();
    private String agentId = "agent0";
    private final Semaphore mainHold = new Semaphore(0);

    private Consul client;
    private KeyValueClient kvClient;
    private KVCache messageCache;
    private ConsulCache.Listener<String, Value> cacheListener;

    private final ScheduledExecutorService sessionRenewPool = Executors.newSingleThreadScheduledExecutor();
    private long sessionRenewSeconds = 4;
    private long sessionTTLSeconds = 10;

    public void init() {
        client = Consul.builder().build();
        kvClient = client.keyValueClient();
        messageCache = KVCache.newCache(kvClient, "mft/agents/messages/" + agentId );
    }

    private void acceptRequests() {
        cacheListener = newValues -> {
            // Cache notifies all paths with "foo" the root path
            // If you want to watch only "foo" value, you must filter other paths

            newValues.values().forEach(value -> {

                // Values are encoded in key/value store, decode it if needed
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    System.out.println(String.format("Value is: %s", v));
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        TransferRequest request = mapper.readValue(v, TransferRequest.class);
                        System.out.println("Received request " + request.getTransferId());

                        Connector inConnector = MFTAgent.this.resolveConnector(request.getSourceType(), "IN");
                        inConnector.init(request.getSourceId(), request.getSourceToken());
                        Connector outConnector = MFTAgent.this.resolveConnector(request.getDestinationType(), "OUT");
                        outConnector.init(request.getDestinationId(), request.getDestinationToken());

                        MetadataCollector metadataCollector = MFTAgent.this.resolveMetadataCollector(request.getSourceType());
                        ResourceMetadata metadata = metadataCollector.getGetResourceMetadata(request.getSourceId(), request.getSourceToken());
                        System.out.println("File size " + metadata.getResourceSize());
                        String transferId = mediator.transfer(inConnector, outConnector, metadata);
                        System.out.println("Submitted transfer " + transferId);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Deleting key " + value.getKey());
                        kvClient.deleteKey(value.getKey()); // Due to bug in consul https://github.com/hashicorp/consul/issues/571
                    }
                });

            });
        };
        messageCache.addListener(cacheListener);
        messageCache.start();
    }

    private boolean connectAgent() {
        ImmutableSession session = ImmutableSession.builder().name(agentId).behavior("delete").ttl(sessionTTLSeconds + "s").build();
        SessionCreatedResponse sessResp = client.sessionClient().createSession(session);
        String lockPath = "mft/agent/live/" + agentId;
        boolean acquired = kvClient.acquireLock(lockPath, sessResp.getId());

        if (acquired) {
            sessionRenewPool.scheduleAtFixedRate(() -> {
                try {
                    client.sessionClient().renewSession(sessResp.getId());
                } catch (ConsulException e) {
                    if (e.getCode() == 404) {
                        stop();
                    }
                    e.printStackTrace();
                } catch (Exception e) {
                    try {
                        boolean status = kvClient.acquireLock(lockPath, sessResp.getId());
                        if (!status) {
                            stop();
                        }
                    } catch (Exception ex) {
                        stop();
                    }
                }
        }, sessionRenewSeconds, sessionRenewSeconds, TimeUnit.SECONDS);
        }

        System.out.println("Lock status " + acquired);
        return acquired;
    }

    public void disconnectAgent() {
        sessionRenewPool.shutdown();
        if (cacheListener != null) {
            messageCache.removeListener(cacheListener);
        }
    }

    public void stop() {
        System.out.println("Stopping Agent " + agentId);
        disconnectAgent();
        mainHold.release();
    }

    public void start() throws Exception {
        System.out.println("Starting Agent");
        init();
        boolean connected = connectAgent();
        if (!connected) {
            throw new Exception("Failed to connect to the cluster");
        }
        acceptRequests();
    }

    public static void main(String args[]) throws Exception {
        MFTAgent agent = new MFTAgent();
        agent.start();
        agent.mainHold.acquire();
        System.out.println("Agent exited");
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
}
