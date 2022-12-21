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

package org.apache.airavata.mft.command.line.sub.odata;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecret;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecretCreateRequest;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorage;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorageCreateRequest;
import org.apache.airavata.mft.resource.stubs.storage.common.SecretForStorage;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageCommonServiceGrpc;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageType;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "add")
public class ODataRemoteAddSubCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Storage Name", required = true)
    private String remoteName;

    @CommandLine.Option(names = {"-U", "--url"}, description = "Base URL for OData Endpoint", required = true)
    private String baseURL;

    @CommandLine.Option(names = {"-u", "--user"}, description = "User Name", required = true)
    private String userName;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Password", required = true)
    private String password;


    @Override
    public Integer call() throws Exception {
        AuthToken authToken = AuthToken.newBuilder().build();

        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        ODataSecret oDataSecret = mftApiClient.getSecretServiceClient().odata().createODataSecret(ODataSecretCreateRequest.newBuilder()
                .setAuthzToken(authToken).setPassword(password).setUserName(userName).build());

        System.out.println("Created the OData secret " + oDataSecret.getSecretId());

        ODataStorage oDataStorage = mftApiClient.getStorageServiceClient().odata().createODataStorage(
                ODataStorageCreateRequest.newBuilder().setName(remoteName).setBaseUrl(baseURL).build());

        System.out.println("Created OData storage " + oDataStorage.getStorageId());

        StorageCommonServiceGrpc.StorageCommonServiceBlockingStub commonStorageClient = mftApiClient.getStorageServiceClient().common();

        commonStorageClient.registerSecretForStorage(SecretForStorage.newBuilder()
                .setStorageId(oDataStorage.getStorageId())
                .setSecretId(oDataSecret.getSecretId())
                .setStorageType(StorageType.ODATA).build());

        System.out.println("Successfully added OData remote endpoint");

        return 0;
    }
}
