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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.TransferCommand;
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.admin.models.TransferState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 mft/agents/messages/{agent-id} -> message
 mft/agent/info/{agent-id} -> agent infos
 mft/agent/live/{agent-id} -> live agent
 mft/transfer/state/{transfer-id} -> transfer state
 */

public class MFTConsulClient {

    private static final Logger logger = LoggerFactory.getLogger(MFTConsulClient.class);

    private Consul client = Consul.builder().build();
    private KeyValueClient kvClient = client.keyValueClient();
    private ObjectMapper mapper = new ObjectMapper();

    public String submitTransfer(TransferRequest transferRequest) throws MFTAdminException{
        try {
            String asStr = mapper.writeValueAsString(transferRequest);
            String transferId = UUID.randomUUID().toString();
            kvClient.putValue("mft/controller/messages/" + transferId, asStr);
            return transferId;
        } catch (JsonProcessingException e) {
            throw new MFTAdminException("Error in serializing transfer request", e);
        }
    }

    public void commandTransferToAgent(String agentId, TransferCommand transferCommand) throws MFTAdminException {
        try {
            submitTransferState(transferCommand.getTransferId(), new TransferState()
            .setState("INITIALIZING")
            .setPercentage(0)
            .setUpdateTimeMils(System.currentTimeMillis())
            .setDescription("Initializing the transfer"));
            String asString = mapper.writeValueAsString(transferCommand);
            kvClient.putValue("mft/agents/messages/"  + agentId + "/" + transferCommand.getTransferId(), asString);
        } catch (JsonProcessingException e) {
            throw new MFTAdminException("Error in serializing transfer request", e);
        }
    }

    public List<AgentInfo> listAgents() {
        List<AgentInfo> agents = new ArrayList<>();
        List<String> keys = kvClient.getKeys("mft/agents/info");
        for (String key : keys) {
            Optional<AgentInfo> agentInfo = getAgentInfo(key.substring(key.lastIndexOf("/") + 1));
            agentInfo.ifPresent(agents::add);
        }
        return agents;
    }

    public Optional<AgentInfo> getAgentInfo(String agentId) {
        Optional<Value> value = kvClient.getValue("mft/agents/info/" + agentId);
        if (value.isPresent()) {
            Value absVal = value.get();
            if (absVal.getValue().isPresent()) {
                String asStr = absVal.getValueAsString().get();
                try {
                    return Optional.of(mapper.readValue(asStr, AgentInfo.class));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Optional.empty();
    }

    public void registerAgent(AgentInfo agentInfo) throws MFTAdminException {
        try {
            String asString = mapper.writeValueAsString(agentInfo);
            kvClient.putValue("mft/agents/info/" + agentInfo.getId(), asString);
        } catch (JsonProcessingException e) {
            throw new MFTAdminException("Error in serializing agent information", e);
        }
    }

    public List<String> getLiveAgentIds() throws MFTAdminException {
        try {
            List<String> keys = kvClient.getKeys("mft/agent/live/");
            return keys.stream().map(key -> key.substring(key.lastIndexOf("/") + 1)).collect(Collectors.toList());
        } catch (ConsulException e) {
            if (e.getCode() == 404) {
                return Collections.emptyList();
            }
            throw new MFTAdminException("Error in fetching live agents", e);
        } catch (Exception e) {
            throw new MFTAdminException("Error in fetching live agents", e);
        }
    }

    public Optional<TransferState> getTransferState(String transferId) throws MFTAdminException {

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
            throw new MFTAdminException("Error in fetching transfer status " + transferId, e);
        } catch (Exception e) {
            throw new MFTAdminException("Error in fetching transfer status " + transferId, e);
        }
    }

    public void submitTransferState(String transferId, TransferState transferState) throws MFTAdminException {
        try {
            List<TransferState> allStates = getTransferStates(transferId);
            System.out.println(allStates);
            allStates.add(transferState);
            String asStr = mapper.writeValueAsString(allStates);
            kvClient.putValue("mft/transfer/state/" + transferId, asStr);

            logger.info("Saved transfer status " + asStr);

        } catch (Exception e) {
            throw new MFTAdminException("Error in serializing transfer status", e);
        }
    }

    public List<TransferState> getTransferStates(String transferId) throws IOException {
        Optional<Value> valueOp = kvClient.getValue("mft/transfer/state/" + transferId);
        List<TransferState> allStates;
        if (valueOp.isPresent()) {
            String prevStates = valueOp.get().getValueAsString().get();
            allStates = new ArrayList<>(Arrays.asList(mapper.readValue(prevStates, TransferState[].class)));
        } else {
            allStates = new ArrayList<>();
        }
        return allStates;
    }

    public List<AgentInfo> getLiveAgentInfos() throws MFTAdminException {
        List<String> liveAgentIds = getLiveAgentIds();
        return liveAgentIds.stream().map(id -> getAgentInfo(id).get()).collect(Collectors.toList());
    }
}
