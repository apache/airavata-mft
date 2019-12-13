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
import com.orbitz.consul.KeyValueClient;
import java.util.Collections;

public class ConsulTester {
    public static void main(String args[]) throws Exception {
        Consul client = Consul.builder().build();
        KeyValueClient kvClient = client.keyValueClient();
        ObjectMapper mapper = new ObjectMapper();

        TransferRequest request = new TransferRequest();
        request.setSourceId("1");
        request.setSourceType("SCP");
        request.setDestinationId("2");
        request.setDestinationType("SCP");
        request.setAgentList(Collections.singletonList("agent0"));
        request.setTransferId("transfer005");

        String asString = mapper.writeValueAsString(request);

        kvClient.putValue("agent0/messages/" + request.getTransferId(), asString);
    }
}
