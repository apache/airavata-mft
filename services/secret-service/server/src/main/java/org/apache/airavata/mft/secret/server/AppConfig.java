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

package org.apache.airavata.mft.secret.server;

import org.apache.airavata.mft.secret.server.backend.custos.auth.AgentAuthenticationHandler;
import org.apache.custos.clients.CustosClientProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AppConfig {


    @Value("${custos.host}")
    private String custosHost;

    @Value("${custos.port}")
    private int custosPort;

    @Value("${custos.id}")
    private String custosId;

    @Value("${custos.secret}")
    private String custosSecret;

    @Bean
    public CustosClientProvider custosClientProvider() {
        return new CustosClientProvider.Builder().setServerHost(custosHost)
                .setServerPort(custosPort)
                .setClientId(custosId)
                .setClientSec(custosSecret).build();
    }

    @Bean
    public AgentAuthenticationHandler agentAuthenticationHandler(CustosClientProvider custosClientProvider) throws IOException {
        return new AgentAuthenticationHandler(this.custosId, custosClientProvider);
    }


}
