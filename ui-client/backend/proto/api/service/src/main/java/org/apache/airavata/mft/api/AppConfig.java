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

package org.apache.airavata.mft.api;

import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.SyncRPCClient;
import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("ASAppConfig")
public class AppConfig {

    @org.springframework.beans.factory.annotation.Value("${consul.host}")
    public String consulHost;

    @org.springframework.beans.factory.annotation.Value("${consul.port}")
    public Integer consulPort;

    @org.springframework.beans.factory.annotation.Value("${api.id}")
    public String apiId;

    @Bean
    public MFTConsulClient mftConsulClient() {
        return new MFTConsulClient(consulHost, consulPort);
    }

    @Bean
    public SyncRPCClient agentRPCClient() {
        SyncRPCClient client = new SyncRPCClient("api-server-" + apiId, mftConsulClient());
        client.init();
        return client;
    }

    @Bean
    public DozerBeanMapper dozerBeanMapper() {
        return new DozerBeanMapper();
    }
}
