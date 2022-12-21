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

package org.apache.airavata.mft.api.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.mft.api.service.*;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.client.SecretServiceClientBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MFTApiClient implements Closeable {

    private ManagedChannel channel;
    private StorageServiceClient storageServiceClient;
    private SecretServiceClient secretServiceClient;

    private String transferServiceHost;
    private int transferServicePort;

    private String resourceServiceHost;
    private int resourceServicePort;

    private String secretServiceHost;
    private int secretServicePort;

    public void init() {
        channel = ManagedChannelBuilder.forAddress(transferServiceHost, transferServicePort).usePlaintext().build();
        storageServiceClient = StorageServiceClientBuilder.buildClient(resourceServiceHost, resourceServicePort);
        secretServiceClient = SecretServiceClientBuilder.buildClient(secretServiceHost, secretServicePort);
    }

    public MFTTransferServiceGrpc.MFTTransferServiceBlockingStub getTransferClient() {
        return MFTTransferServiceGrpc.newBlockingStub(channel);
    }

    public StorageServiceClient getStorageServiceClient() {
        return storageServiceClient;
    }

    public SecretServiceClient getSecretServiceClient() {
        return secretServiceClient;
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.shutdown();
        }
    }

    public static final class MFTApiClientBuilder {
        private String transferServiceHost = "localhost";
        private int transferServicePort = 7004;
        private String resourceServiceHost = "localhost";
        private int resourceServicePort = 7002;
        private String secretServiceHost = "localhost";
        private int secretServicePort = 7003;

        private MFTApiClientBuilder() {
        }

        public static MFTApiClientBuilder newBuilder() {
            return new MFTApiClientBuilder();
        }

        public MFTApiClientBuilder withTransferServiceHost(String transferServiceHost) {
            this.transferServiceHost = transferServiceHost;
            return this;
        }

        public MFTApiClientBuilder withTransferServicePort(int transferServicePort) {
            this.transferServicePort = transferServicePort;
            return this;
        }

        public MFTApiClientBuilder withResourceServiceHost(String resourceServiceHost) {
            this.resourceServiceHost = resourceServiceHost;
            return this;
        }

        public MFTApiClientBuilder withResourceServicePort(int resourceServicePort) {
            this.resourceServicePort = resourceServicePort;
            return this;
        }

        public MFTApiClientBuilder withSecretServiceHost(String secretServiceHost) {
            this.secretServiceHost = secretServiceHost;
            return this;
        }

        public MFTApiClientBuilder withSecretServicePort(int secretServicePort) {
            this.secretServicePort = secretServicePort;
            return this;
        }

        public MFTApiClient build() {
            MFTApiClient mFTApiClient = new MFTApiClient();
            mFTApiClient.transferServicePort = this.transferServicePort;
            mFTApiClient.transferServiceHost = this.transferServiceHost;
            mFTApiClient.secretServicePort = this.secretServicePort;
            mFTApiClient.resourceServicePort = this.resourceServicePort;
            mFTApiClient.secretServiceHost = this.secretServiceHost;
            mFTApiClient.resourceServiceHost = this.resourceServiceHost;
            mFTApiClient.init();
            return mFTApiClient;
        }
    }
}
