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

import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.agent.http.HttpServer;
import org.apache.airavata.mft.agent.http.HttpTransferRequestsStore;
import org.apache.airavata.mft.api.service.CallbackEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MFTAgent implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MFTAgent.class);

    @org.springframework.beans.factory.annotation.Value("${agent.id}")
    private String agentId;

    @org.springframework.beans.factory.annotation.Value("${agent.host}")
    private String agentHost;

    @org.springframework.beans.factory.annotation.Value("${agent.http.port}")
    private Integer agentHttpPort;

    @org.springframework.beans.factory.annotation.Value("${agent.https.enabled}")
    private boolean agentHttpsEnabled;

    private final Semaphore mainHold = new Semaphore(0);

    @Autowired
    private HttpTransferRequestsStore transferRequestsStore;

    public void init() {

    }

    private void handleCallbacks(List<CallbackEndpoint> callbackEndpoints, String transferId, TransferState transferState) {
        if (callbackEndpoints != null && !callbackEndpoints.isEmpty()) {
            for (CallbackEndpoint cbe : callbackEndpoints) {
                switch (cbe.getType()) {
                    case HTTP:
                        break;
                    case KAFKA:
                        break;
                }
            }
        }
    }

    private void acceptHTTPRequests() {
        logger.info("Starting the HTTP front end");

        new Thread(() -> {
            HttpServer httpServer = new HttpServer(agentHost, agentHttpPort, agentHttpsEnabled, transferRequestsStore);
            try {
                httpServer.run();
            } catch (Exception e) {
                logger.error("Http frontend server start failed", e);
            }
        }).start();
    }

    @PreDestroy
    public void stop() {
        logger.info("Stopping Agent " + agentId);
        mainHold.release();
    }

    public void start() throws Exception {
        init();
        acceptHTTPRequests();
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Agent " + agentId);
        start();
        mainHold.acquire();
        logger.info("Agent exited");
    }

    public static void main(String args[]) throws Exception {
        SpringApplication.run(MFTAgent.class);
    }
}
