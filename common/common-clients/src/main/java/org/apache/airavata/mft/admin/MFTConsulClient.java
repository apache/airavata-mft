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
import org.apache.airavata.mft.agent.stub.EndpointPaths;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.net.HostAndPort.fromParts;

/*
 mft/agents/messages/{agent-id} -> message
 mft/agent/info/{agent-id} -> agent infos
 mft/agent/live/{agent-id} -> live agent
 mft/transfer/state/{transfer-id} -> transfer state
 */

@Component
public class MFTConsulClient {

    private static final Logger logger = LoggerFactory.getLogger(MFTConsulClient.class);

    private Consul client;
    private KeyValueClient kvClient;
    private SessionClient sessionClient;
    private ObjectMapper mapper = new ObjectMapper();

    public static final String TRANSFER_STATE_PATH = "mft/transfer/state/";
    public static final String AGENTS_RPC_REQUEST_MESSAGE_PATH = "mft/agents/rpcmessages/";
    public static final String AGENTS_INFO_PATH = "mft/agents/info/";
    public static final String LIVE_AGENTS_PATH = "mft/agent/live/";

    public static final String TRANSFER_PENDING_PATH = "mft/transfer/pending/";
    public static final String TRANSFER_PROCESSED_PATH = "mft/transfer/processed/";
    public static final String AGENTS_TRANSFER_REQUEST_MESSAGE_PATH = "mft/agents/transfermessages/";
    public static final String AGENTS_SCHEDULED_PATH = "mft/agents/scheduled/";

    public static final String CONTROLLER_STATE_MESSAGE_PATH = "mft/controller/messages/states/";
    public static final String CONTROLLER_TRANSFER_MESSAGE_PATH = "mft/controller/messages/transfers/";

    /* Transfer lifecycle:
         1. pending: Controller picks up the transfer request from API
                Example path : mft/transfer/pending/{transferId} -> TransferApiRequest
         2. processed: Controller processed the transfer and handed over it to the Agent/s
                Example path: mft/transfer/processed/{transferId} -> TransferApiRequest
         3. agent_transfer_request: Pass the agent transfer request to agent from controller
                Example paths
                mft/agents/transfermessages/{agentId_1}/{transferId}/{requestId_1} -> AgentTransferRequest_1
                mft/agents/transfermessages/{agentId_2}/{transferId}/{requestId_2} -> AgentTransferRequest_2
         4. scheduled: Once agent reads agent_transfer_request, it creates a path to make a note that it received the message.
                       Once the transfer is completed or failed, this path is deleted
                mft/agents/scheduled/{agentId}/{agentSession}/{transferId}/{requestId_1} -> Empty Node

                If the agent was restarted while a transfer is being processed, this path is used to recover the state
         5. Agent publishes the transfer state to controller
                mft/controller/messages/states/{transferId}/{agentId}/{requestId}/{md5sum(sourcePath:destinationPath)}/{timeMils()} -> Transfer State
         6. Controller saves the transfer state in
                mft/transfer/state/{transferId}/{requestId}/{UUID} -> Transfer State

     */

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

    public MFTConsulClient() {
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
            byte[] transferReqBytes = transferRequest.toByteArray();
            kvClient.putValue(AGENTS_TRANSFER_REQUEST_MESSAGE_PATH + agentId + "/" + transferId + "/" + transferRequest.getRequestId(), transferReqBytes,
                    0L, PutOptions.BLANK);

        } catch (Exception e) {
            throw new MFTConsulClientException("Error in submitting transfer command to Agent through consul", e);
        }
    }

    public List<String> listPendingAgentTransfers(String agentId) throws MFTConsulClientException  {
        try {
            try {
                return kvClient.getKeys(AGENTS_TRANSFER_REQUEST_MESSAGE_PATH + agentId);
            } catch (ConsulException e) {
                if (e.getCode() == 404) {
                    return Collections.emptyList();
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new MFTConsulClientException("Failed to list pending agent transfers for agent " + agentId, e);
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
    public void submitFileTransferStateToProcess(String transferId, String agentRequestId,
                                                 EndpointPaths endpointPath,
                                                 String agentId, TransferState transferState) throws MFTConsulClientException {
        try {

            String pathMD5 = getEndpointPathHash(endpointPath);
            kvClient.putValue(CONTROLLER_STATE_MESSAGE_PATH + transferId + "/" + agentId + "/" + agentRequestId + "/" + pathMD5 + "/" + transferState.getUpdateTimeMils(),
                        mapper.writeValueAsString(transferState));
        } catch (Exception e) {
            logger.error("Error in submitting transfer status to process for transfer {} and agent {}", transferId, agentId, e);
            throw new MFTConsulClientException("Error in submitting transfer status", e);
        }
    }

    public String getEndpointPathHash(EndpointPaths endpointPath) {
        return DigestUtils.md5DigestAsHex((endpointPath.getSourcePath() + ":" + endpointPath.getDestinationPath()).getBytes());
    }

    public String getEndpointPathHash(org.apache.airavata.mft.api.service.EndpointPaths endpointPath) {
        return DigestUtils.md5DigestAsHex((endpointPath.getSourcePath() + ":" + endpointPath.getDestinationPath()).getBytes());
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
    public void saveTransferState(String transferId, String agentRequestId, TransferState transferState) throws MFTConsulClientException {
        try {
            String asStr = mapper.writeValueAsString(transferState);
            if (agentRequestId == null) {
                kvClient.putValue(TRANSFER_STATE_PATH + transferId + "/" + UUID.randomUUID().toString(), asStr);
            } else {
                kvClient.putValue(TRANSFER_STATE_PATH + transferId + "/" + agentRequestId + "/" + UUID.randomUUID().toString(), asStr);
            }
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
    public Optional<TransferState> getLastTransferState(String transferId) throws MFTConsulClientException {

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
        return getTransferStates(transferId, null);
    }

    public List<TransferState> getTransferStates(String transferId, String agentRequestId) throws IOException {
        List<String> keys = kvClient.getKeys(TRANSFER_STATE_PATH + transferId + (agentRequestId == null? "" : "/" + agentRequestId));

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

    public void markTransferAsProcessed(String transferId, TransferApiRequest transferRequest) {
        kvClient.putValue(MFTConsulClient.TRANSFER_PROCESSED_PATH + transferId,
                transferRequest.toByteArray(), 0L, PutOptions.BLANK);
    }
    public Optional<TransferApiRequest> getProcessedTransfer(String transferId) throws InvalidProtocolBufferException {
        Optional<Value> value = kvClient.getValue(TRANSFER_PROCESSED_PATH + transferId);
        if (value.isPresent()) {
            return Optional.of(TransferApiRequest.newBuilder().mergeFrom(value.get().getValueAsBytes().get()).build());
        } else {
            return Optional.empty();
        }
    }

    public List<AgentInfo> getLiveAgentInfos() throws MFTConsulClientException {
        List<String> liveAgentIds = getLiveAgentIds();
        return liveAgentIds.stream().map(id -> getAgentInfo(id).get()).collect(Collectors.toList());
    }

    public void createEndpointHookForAgent(String agentId, String session, String transferId,
                                           String agentTransferRequestId,
                                           EndpointPaths endpointPaths) {
        getKvClient().putValue(MFTConsulClient.AGENTS_SCHEDULED_PATH
                        + agentId + "/" + session + "/" + transferId + "/" + agentTransferRequestId
                        + "/" + getEndpointPathHash(endpointPaths),
                endpointPaths.toByteArray(), 0L, PutOptions.BLANK);
    }

    public void deleteEndpointHookForAgent(String agentId, String session, String transferId,
                                          String agentTransferRequestId,
                                          EndpointPaths endpointPaths) {
        getKvClient().deleteKey(MFTConsulClient.AGENTS_SCHEDULED_PATH
                + agentId + "/" + session + "/" + transferId + "/" + agentTransferRequestId
                + "/" + getEndpointPathHash(endpointPaths));
    }

    public int getEndpointHookCountForAgent(String agentId) throws MFTConsulClientException {
        Optional<String> session = getKvClient().getSession(LIVE_AGENTS_PATH + agentId);

        try {
            try {
                return session.map(s -> getKvClient().getKeys(MFTConsulClient.AGENTS_SCHEDULED_PATH
                        + agentId + "/" + s).size()).orElse(0);
            } catch (ConsulException e) {
                if (e.getCode() == 404) {
                    return 0;
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            throw new MFTConsulClientException("Failed to fetch endpoint hook count for agent " + agentId, e);
        }
    }

    public KeyValueClient getKvClient() {
        return kvClient;
    }

    public SessionClient getSessionClient() {
        return sessionClient;
    }
}
