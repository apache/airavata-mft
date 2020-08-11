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

 package org.apache.airavata.mft.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * This is the client implementation to provide RPC (Request / Response) based communication to Agents through the Consul
 * Key Value store. Using this mechanism, we can invoke methods of the Agents which are living in both private and public
 * networks.
 */
public class SyncRPCClient {

    private static final Logger logger = LoggerFactory.getLogger(MFTConsulClient.class);

    private String baseResponsePath;

    private ConsulCache.Listener<String, Value> syncResponseCacheListener;
    private KVCache syncResponseCache;
    private MFTConsulClient mftConsulClient;

    private ObjectMapper mapper = new ObjectMapper();
    private Map<String, ArrayBlockingQueue<SyncRPCResponse>> responseQueueMap = new ConcurrentHashMap<>();

    public SyncRPCClient(String agentId, MFTConsulClient mftConsulClient) {
        this.baseResponsePath = "mft/sync/response/" + agentId + "/";
        this.mftConsulClient = mftConsulClient;
    }

    public void init() {
        syncResponseCache = KVCache.newCache(mftConsulClient.getKvClient(), baseResponsePath);
        listenToResponses();
    }

    public void disconnectClient() {
        if (syncResponseCacheListener != null) {
            syncResponseCache.removeListener(syncResponseCacheListener);
        }
    }

    private void listenToResponses() {
        syncResponseCacheListener = newValues -> {
            newValues.values().forEach(value -> {
                Optional<String> decodedValue = value.getValueAsString();
                decodedValue.ifPresent(v -> {
                    try {
                        SyncRPCResponse response = mapper.readValue(v, SyncRPCResponse.class);
                        if (responseQueueMap.containsKey(response.getMessageId())) {
                            responseQueueMap.get(response.getMessageId()).put(response);
                        }
                    } catch (Throwable e) {
                        logger.error("Errored while processing sync response", e);
                    } finally {
                        mftConsulClient.getKvClient().deleteKey(value.getKey());
                    }
                });
            });
        };

        syncResponseCache.addListener(syncResponseCacheListener);
        syncResponseCache.start();
    }

    public SyncRPCResponse sendSyncRequest(SyncRPCRequest request, long waitMs) throws MFTConsulClientException, InterruptedException {
        request.setReturnAddress(this.baseResponsePath + request.getMessageId());
        ArrayBlockingQueue<SyncRPCResponse> queue = new ArrayBlockingQueue<>(1);

        this.responseQueueMap.put(request.getMessageId(), queue);

        try {
            this.mftConsulClient.sendSyncRPCToAgent(request.getAgentId(), request);
            SyncRPCResponse response = queue.poll(waitMs, TimeUnit.MILLISECONDS);
            if (response == null) {
                throw new MFTConsulClientException("Timed out waiting for the response");
            }
            return response;
        } finally {
            this.responseQueueMap.remove(request.getMessageId());
        }
    }

    public SyncRPCResponse sendSyncRequest(SyncRPCRequest request) throws MFTConsulClientException, InterruptedException {
        return sendSyncRequest(request, 10000);
    }
}
