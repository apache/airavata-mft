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

 package org.apache.airavata.mft.admin.models.rpc;

import java.util.Map;

public class SyncRPCRequest {
    private String agentId;
    private String method;
    private Map<String, String> parameters;
    private String returnAddress;
    private String messageId;

    public String getAgentId() {
        return agentId;
    }

    public SyncRPCRequest setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public String getMethod() {
        return method;
    }

    public SyncRPCRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public SyncRPCRequest setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public String getReturnAddress() {
        return returnAddress;
    }

    public SyncRPCRequest setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public SyncRPCRequest setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }
}
