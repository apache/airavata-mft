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

package org.apache.airavata.mft.command.line.sub.swift;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.credential.stubs.swift.SwiftPasswordSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecretCreateRequest;
import org.apache.airavata.mft.resource.stubs.storage.common.SecretForStorage;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageCommonServiceGrpc;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageType;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageCreateRequest;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "add")
public class SwiftAddSubCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Storage Name", required = true)
    private String remoteName;

    @CommandLine.Option(names = {"-c", "--container"}, description = "Swift Container Name", required = true)
    private String container;

    @CommandLine.Option(names = {"-e", "--endpoint"}, description = "Endpoint Name", required = true)
    private String endpoint;

    @CommandLine.Option(names = {"-r", "--region"}, description = "Region", required = true)
    private String region;

    @CommandLine.Option(names = {"-v", "--keystoneversion"}, description = "Keystone Version", required = true)
    private int keystoneVersion;

    @CommandLine.Option(names = {"-u", "--user"}, description = "User Name (Password Credentials", required = true)
    private String userName;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Password (Password Credentials", required = true)
    private String password;

    @CommandLine.Option(names = {"-pid", "--projectId"}, description = "Project Id (Password Credentials", required = true)
    private String projectId;

    @CommandLine.Option(names = {"-d", "--domainId"}, description = "Domain Id (Password Credentials", required = true)
    private String domainId;


    @Override
    public Integer call() throws Exception {
        AuthToken authToken = AuthToken.newBuilder().build();

        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        SwiftSecret swiftSecret = mftApiClient.getSecretServiceClient().swift().createSwiftSecret(SwiftSecretCreateRequest.newBuilder()
                .setAuthzToken(authToken).setPasswordSecret(SwiftPasswordSecret.newBuilder()
                        .setUserName(userName)
                        .setPassword(password)
                        .setProjectId(projectId)
                        .setDomainId(domainId).build()).build());

        System.out.println("Created the swift secret " + swiftSecret.getSecretId());

        SwiftStorage swiftStorage = mftApiClient.getStorageServiceClient().swift().createSwiftStorage(SwiftStorageCreateRequest.newBuilder()
                .setName(remoteName)
                .setContainer(container)
                .setEndpoint(endpoint)
                .setKeystoneVersion(keystoneVersion)
                .setRegion(region).build());

        System.out.println("Created swift storage " + swiftStorage.getStorageId());

        StorageCommonServiceGrpc.StorageCommonServiceBlockingStub commonClient = mftApiClient.getStorageServiceClient().common();
        commonClient.registerSecretForStorage(SecretForStorage.newBuilder()
                .setStorageId(swiftStorage.getStorageId())
                .setSecretId(swiftSecret.getSecretId())
                .setStorageType(StorageType.SWIFT).build());

        System.out.println("Successfully added Swift remote endpoint");

        return 0;
    }
}
