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
import com.orbitz.consul.Consul;
import com.orbitz.consul.ConsulException;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.model.kv.Value;
import org.apache.airavata.mft.admin.models.AgentInfo;
import org.apache.airavata.mft.admin.models.TransferRequest;
import org.apache.airavata.mft.admin.models.TransferState;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*
 mft/agents/messages/{agent-id} -> message
 mft/agent/info/{agent-id} -> agent infos
 mft/agent/live/{agent-id} -> live agent
 mft/transfer/state/{transfer-id} -> transfer state
 */

public class MFTAdmin {

    private Consul client = Consul.builder().build();
    private KeyValueClient kvClient = client.keyValueClient();
    private ObjectMapper mapper = new ObjectMapper();

    public String submitTransfer(String agentId, TransferRequest transferRequest) throws MFTAdminException {
        try {

            String transferId = UUID.randomUUID().toString();
            updateTransferState(transferId, new TransferState().setState("INITIALIZING").setPercentage(0).setUpdateTimeMils(System.currentTimeMillis()));
            transferRequest.setTransferId(transferId);
            String asString = mapper.writeValueAsString(transferRequest);
            kvClient.putValue("mft/agents/messages/"  + agentId + "/" + transferId, asString);
            return transferId;
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

    public TransferState getTransferState(String transferId) throws MFTAdminException {
        try {
            Optional<Value> value = kvClient.getValue("mft/transfer/state/" + transferId);
            if (value.isPresent()) {
                if (value.get().getValueAsString().isPresent()) {
                    String asStr = value.get().getValueAsString().get();
                    return mapper.readValue(asStr, TransferState.class);
                }
            }
            return new TransferState().setPercentage(0).setUpdateTimeMils(System.currentTimeMillis()).setState("UNKNOWN");

        } catch (ConsulException e) {
            if (e.getCode() == 404) {
                return new TransferState().setPercentage(0).setUpdateTimeMils(System.currentTimeMillis()).setState("UNKNOWN");
            }
            throw new MFTAdminException("Error in fetching transfer status " + transferId, e);
        } catch (Exception e) {
            throw new MFTAdminException("Error in fetching transfer status " + transferId, e);
        }
    }

    public void updateTransferState(String transferId, TransferState transferState) throws MFTAdminException {
        try {
            String asStr = mapper.writeValueAsString(transferState);
            kvClient.putValue("mft/transfer/state/" + transferId, asStr);
        } catch (JsonProcessingException e) {
            throw new MFTAdminException("Error in serializing transfer status", e);
        }
    }

    public List<AgentInfo> getLiveAgentInfos() throws MFTAdminException {
        List<String> liveAgentIds = getLiveAgentIds();
        return liveAgentIds.stream().map(id -> getAgentInfo(id).get()).collect(Collectors.toList());
    }
}
