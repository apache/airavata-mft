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

package org.apache.airavata.mft.command.line.sub.transfer;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.EndpointPaths;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.api.service.TransferApiResponse;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.resource.stubs.storage.common.Error;
import org.apache.airavata.mft.resource.stubs.storage.common.SecretForStorage;
import org.apache.airavata.mft.resource.stubs.storage.common.SecretForStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageCommonServiceGrpc;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "submit", description = "Submit a data transfer job")
public class SubmitTransferSubCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-s", "--source"}, description = "Source Storage Id", required = true)
    private String sourceStorageId;

    @CommandLine.Option(names = {"-d", "--destination"}, description = "Destination Storage Id", required = true)
    private String destinationStorageId;

    @CommandLine.Option(names = {"-sp", "--source-path"}, description = "Source Path", required = true)
    private String sourcePath;

    @CommandLine.Option(names = {"-dp", "--destination-path"}, description = "Destination Path", required = true)
    private String destinationPath;

    @Override
    public Integer call() throws Exception {
        System.out.println("Transferring data from " + sourceStorageId + " to " + destinationStorageId);
        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        AuthToken token = AuthToken.newBuilder().build();

        StorageCommonServiceGrpc.StorageCommonServiceBlockingStub commonClient = mftApiClient.getStorageServiceClient().common();
        SecretForStorage sourceSecretForStorage = commonClient
                .getSecretForStorage(SecretForStorageGetRequest.newBuilder().setStorageId(sourceStorageId).build());

        if (sourceSecretForStorage.getError() != Error.UNRECOGNIZED) {
            System.out.println("Errored while fetching credentials for source storage " + sourceStorageId
                    + ". Error: " + sourceSecretForStorage.getError());
        }

        SecretForStorage destSecretForStorage = commonClient
                .getSecretForStorage(SecretForStorageGetRequest.newBuilder().setStorageId(destinationStorageId).build());

        if (destSecretForStorage.getError() != Error.UNRECOGNIZED) {
            System.out.println("Errored while fetching credentials for destination storage " + sourceStorageId
                    + ". Error: " + destSecretForStorage.getError());
        }

        TransferApiResponse transferResp = mftApiClient.getTransferClient().submitTransfer(TransferApiRequest.newBuilder()
                .setSourceSecretId(sourceSecretForStorage.getSecretId())
                .setDestinationSecretId(destSecretForStorage.getSecretId())
                .setDestinationStorageId(destinationStorageId)
                .setSourceStorageId(sourceStorageId)
                .addEndpointPaths(EndpointPaths.newBuilder().setSourcePath(sourcePath)
                        .setDestinationPath(destinationPath).build()).build());

        System.out.println("Submitted Transfer " + transferResp.getTransferId());
        return 0;
    }
}
