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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.PutOptions;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCRequest;
import org.apache.airavata.mft.admin.models.rpc.SyncRPCResponse;
import org.apache.airavata.mft.agent.stub.AgentTransferRequest;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.net.HostAndPort.*;
import static com.google.common.net.HostAndPort.fromParts;

/*
 mft/agents/messages/{agent-id} -> message
 mft/agent/info/{agent-id} -> agent infos
 mft/agent/live/{agent-id} -> live agent
 mft/transfer/state/{transfer-id} -> transfer state
 */

public class MFTConsulClient {

    private static final Logger logger = LoggerFactory.getLogger(MFTConsulClient.class);

    private Consul client;
    private KeyValueClient kvClient;
    private SessionClient sessionClient;
    private ObjectMapper mapper = new ObjectMapper();

    public static final String TRANSFER_STATE_PATH = "mft/transfer/state/";
    public static final String CONTROLLER_TRANSFER_MESSAGE_PATH = "mft/controller/messages/transfers/";
    public static final String CONTROLLER_STATE_MESSAGE_PATH = "mft/controller/messages/states/";
    public static final String AGENTS_TRANSFER_REQUEST_MESSAGE_PATH = "mft/agents/transfermessages/";
    public static final String AGENTS_RPC_REQUEST_MESSAGE_PATH = "mft/agents/rpcmessages/";
    public static final String AGENTS_SCHEDULED_PATH = "mft/agents/scheduled/";
    public static final String AGENTS_INFO_PATH = "mft/agents/info/";
    public static final String LIVE_AGENTS_PATH = "mft/agent/live/";
    public static final String TRANSFER_PROCESSED_PATH = "mft/transfer/processed/";
    public static final String TRANSFER_PENDING_PATH = "mft/transfer/pending/";

    public MFTConsulClient(Map<String, Integer> consulHostPorts) {
        List<HostAndPort> hostAndPorts = consulHostPorts.entrySet().stream()
                .map(entry -> fromParts(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        this.client = Consul.builder().withMultipleHostAndPort(hostAndPorts, 100000).build();
        this.kvClient = client.keyValueClient();
        this.sessionClient = client.sessionClient();
    }

    public MFTConsulClient(String host, int port) {
        this.client = Consul.builder().withHostAndPort(HostAndPort.fromParts(host, port)).build();
        this.kvClient = client.keyValueClient();
        this.sessionClient = client.sessionClient();
    }

    public String submitTransfer(TransferApiRequest transferRequest) throws MFTConsulClientException {
        try {
            String transferId = UUID.randomUUID().toString();
            kvClient.putValue(CONTROLLER_TRANSFER_MESSAGE_PATH + transferId, transferRequest.toByteArray(),
                    0L, PutOptions.BLANK);
            return transferId;
        } catch (Exception e) {
            throw new MFTConsulClientException("Error in serializing transfer request", e);
        }
    }

    /**
     * Submits a {@link TransferApiRequest} to a target agent
     *
     * @param agentId Agent Id
     * @param transferRequest Target transfer request
     * @throws MFTConsulClientException If {@link TransferApiRequest} can not be delivered to consul store
     */
    public void commandTransferToAgent(String agentId, String transferId, AgentTransferRequest transferRequest)
            throws MFTConsulClientException {
        try {
            submitTransferStateToProcess(transferId, "controller", new TransferState()
            .setState("INITIALIZING")
            .setPercentage(0)
            .setUpdateTimeMils(System.currentTimeMillis())
            .setPublisher("controller")
            .setDescription("Initializing the transfer"));
            byte[] transferReqBytes = transferRequest.toByteArray();
            kvClient.putValue(AGENTS_TRANSFER_REQUEST_MESSAGE_PATH + agentId + "/" + transferId, transferReqBytes,
                    0L, PutOptions.BLANK);

        } catch (Exception e) {
            throw new MFTConsulClientException("Error in submitting transfer command to Agent through consul", e);
        }
    }

    public void sendSyncRPCToAgent(String agentId, SyncRPCRequest rpcRequest) throws MFTConsulClientException {
        try {
            String asString = mapper.writeValueAsString(rpcRequest);
            kvClient.putValue(AGENTS_RPC_REQUEST_MESSAGE_PATH + agentId + "/" + rpcRequest.getMessageId(), asString);
        } catch (JsonProcessingException e) {
            throw new MFTConsulClientException("Error in serializing rpc request", e);
        }
    }

    public void sendSyncRPCResponseFromAgent(String returnAddress, SyncRPCResponse rpcResponse) throws MFTConsulClientException {
        try {
            String asString = mapper.writeValueAsString(rpcResponse);
            kvClient.putValue(returnAddress, asString);
        } catch (JsonProcessingException e) {
            throw new MFTConsulClientException("Error in serializing rpc response", e);
        }
    }

    /**
     * List all currently registered agents.
     *
     * @return A list of {@link AgentInfo}
     */
    public List<AgentInfo> listAgents() {
        List<AgentInfo> agents = new ArrayList<>();
        List<String> keys = kvClient.getKeys(AGENTS_INFO_PATH);
        for (String key : keys) {
            Optional<AgentInfo> agentInfo = getAgentInfo(key.substring(key.lastIndexOf("/") + 1));
            agentInfo.ifPresent(agents::add);
        }
        return agents;
    }

    /**
     * Get the {@link AgentInfo} for a given agent id
     * @param agentId Agent Id
     * @return AgentInfo if such agent is available
     */
    public Optional<AgentInfo> getAgentInfo(String agentId) {
        Optional<Value> value = kvClient.getValue(AGENTS_INFO_PATH + agentId);
        if (value.isPresent()) {
            Value absVal = value.get();
            if (absVal.getValue().isPresent()) {
                String asStr = absVal.getValueAsString().get();
                try {
                    return Optional.of(mapper.readValue(asStr, AgentInfo.class));
                } catch (IOException e) {
                    logger.error("Errored while fetching agent {} info", agentId, e);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Agents are supposed to register themselves in MFT using this method
     *
     * @param agentInfo {@link AgentInfo} of the source Agents
     * @throws MFTConsulClientException If {@link AgentInfo} can not be saved in consul store
     */
    public void registerAgent(AgentInfo agentInfo) throws MFTConsulClientException {
        try {
            String asString = mapper.writeValueAsString(agentInfo);
            kvClient.putValue(AGENTS_INFO_PATH + agentInfo.getId(), asString);
        } catch (JsonProcessingException e) {
            throw new MFTConsulClientException("Error in serializing agent information", e);
        }
    }

    /**
     * List all currently live agents
     *
     * @return A list of live agent ids
     * @throws MFTConsulClientException If live agents can not be fetched from consul store
     */
    public List<String> getLiveAgentIds() throws MFTConsulClientException {
        try {
            List<String> keys = kvClient.getKeys(LIVE_AGENTS_PATH);
            return keys.stream().map(key -> key.substring(key.lastIndexOf("/") + 1)).collect(Collectors.toList());
        } catch (ConsulException e) {
            if (e.getCode() == 404) {
                return Collections.emptyList();
            }
            throw new MFTConsulClientException("Error in fetching live agents", e);
        } catch (Exception e) {
            throw new MFTConsulClientException("Error in fetching live agents", e);
        }
    }

    /**
     * Lists all currently processing transfer id for the given agent
     *
     * @param agentInfo
     * @return
     * @throws MFTConsulClientException
     */
    public List<String> getAgentActiveTransferIds(AgentInfo agentInfo) throws MFTConsulClientException {
        try {
            List<String> keys = kvClient.getKeys(MFTConsulClient.AGENTS_SCHEDULED_PATH + agentInfo.getId() + "/" + agentInfo.getSessionId());
            return keys.stream().map(key -> key.substring(key.lastIndexOf("/") + 1)).collect(Collectors.toList());
        } catch (ConsulException e) {
            if (e.getCode() == 404) {
                return Collections.emptyList();
            }
            throw new MFTConsulClientException("Error in fetching active transfers for agent " + agentInfo.getId(), e);
        } catch (Exception e) {
            throw new MFTConsulClientException("Error in fetching active transfers for agent " + agentInfo.getId(), e);
        }
    }

    /**
     * Agents should call this method to submit {@link TransferState}. These status are received by the controller and reorder
     * status messages and put in the final status array.
     *
     * @param transferId
     * @param agentId
     * @param transferState
     * @throws MFTConsulClientException
     */
    public void submitTransferStateToProcess(String transferId, String agentId, TransferState transferState) throws MFTConsulClientException {
        try {
            kvClient.putValue(CONTROLLER_STATE_MESSAGE_PATH + transferId + "/" + agentId + "/" + transferState.getUpdateTimeMils(),
                        mapper.writeValueAsString(transferState));
        } catch (Exception e) {
            logger.error("Error in submitting transfer status to process for transfer {} and agent {}", transferId, agentId, e);
            throw new MFTConsulClientException("Error in submitting transfer status", e);
        }
    }

    /**
     * Add the {@link TransferState} to the aggregated state array. This method should only be called by the
     * Controller and API server once the transfer is accepted. Agents should NEVER call this method as it would corrupt
     * state array when multiple clients are writing at the same time
     *
     * @param transferId
     * @param transferState
     * @throws MFTConsulClientException
     */
    public void saveTransferState(String transferId, TransferState transferState) throws MFTConsulClientException {
        try {
            String asStr = mapper.writeValueAsString(transferState);
            kvClient.putValue(TRANSFER_STATE_PATH + transferId + "/" + UUID.randomUUID().toString(), asStr);
            logger.info("Saved transfer status " + asStr);

        } catch (Exception e) {
            throw new MFTConsulClientException("Error in serializing transfer status", e);
        }
    }

    /**
     * Get the latest {@link TransferState} for given transfer id
     *
     * @param transferId Transfer Id
     * @return Optional {@link TransferState } is there is any
     * @throws MFTConsulClientException
     */
    public Optional<TransferState> getTransferState(String transferId) throws MFTConsulClientException {

        try {
            List<TransferState> states = getTransferStates(transferId);

            Optional<TransferState> lastStatusOp = states.stream().min((o1, o2) -> {
                if (o1.getUpdateTimeMils() == o2.getUpdateTimeMils()) {
                    return 0;
                } else {
                    return o1.getUpdateTimeMils() - o2.getUpdateTimeMils() < 0 ? 1 : -1;
                }
            });

            return lastStatusOp;

        } catch (ConsulException e) {
            throw new MFTConsulClientException("Error in fetching transfer status " + transferId, e);
        } catch (Exception e) {
            throw new MFTConsulClientException("Error in fetching transfer status " + transferId, e);
        }
    }

    /**
     * Provide all {@link TransferState} for given transfer id
     *
     * @param transferId Transfer Id
     * @return The list of all {@link TransferState}
     * @throws IOException
     */
    public List<TransferState> getTransferStates(String transferId) throws IOException {
        List<String> keys = kvClient.getKeys(TRANSFER_STATE_PATH + transferId);

        List<TransferState> allStates = new ArrayList<>();

        for (String key: keys) {
            Optional<Value> valueOp = kvClient.getValue(key);
            String stateAsStr = valueOp.get().getValueAsString().get();
            TransferState transferState = mapper.readValue(stateAsStr, TransferState.class);
            allStates.add(transferState);
        }
        List<TransferState> sortedStates = allStates.stream().sorted((o1, o2) ->
                (o1.getUpdateTimeMils() - o2.getUpdateTimeMils()) < 0 ? -1 :
                (o1.getUpdateTimeMils() - o2.getUpdateTimeMils()) == 0 ? 0 : 1).collect(Collectors.toList());
        return sortedStates;
    }

    public List<AgentInfo> getLiveAgentInfos() throws MFTConsulClientException {
        List<String> liveAgentIds = getLiveAgentIds();
        return liveAgentIds.stream().map(id -> getAgentInfo(id).get()).collect(Collectors.toList());
    }

    public KeyValueClient getKvClient() {
        return kvClient;
    }

    public SessionClient getSessionClient() {
        return sessionClient;
    }
}
