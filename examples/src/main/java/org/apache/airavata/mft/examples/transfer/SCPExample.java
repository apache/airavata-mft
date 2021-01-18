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

 package org.apache.airavata.mft.examples.transfer;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.*;

import java.util.Iterator;

public class SCPExample {
    public static void main(String args[]) throws Exception {
        MFTApiServiceGrpc.MFTApiServiceBlockingStub client = MFTApiClient.buildClient("localhost", 7004);

        String sourceStorageId = "remote-ssh-storage-1";
        String sourceResourcePath = "/tmp/1mb.txt";
        String sourceToken = "ssh-cred-1";
        String destStorageId = "remote-ssh-storage-2";
        String destResourcePath = "/tmp/1mb-copy.txt";
        String destToken = "ssh-cred-2";
        String mftAuthorizationToken = "43ff79ac-e4f2-473c-9ea1-04eee9509a53";

        TransferApiRequest request = TransferApiRequest.newBuilder()
                .setMftAuthorizationToken(mftAuthorizationToken)
                .setSourceStorageId(sourceStorageId)
                .setSourcePath(sourceResourcePath)
                .setSourceToken(sourceToken)
                .setSourceType("SCP")
                .setDestinationStorageId(destStorageId)
                .setDestinationPath(destResourcePath)
                .setDestinationToken(destToken)
                .setDestinationType("SCP")
                .setAffinityTransfer(false).build();

        TransferApiResponse transferApiResponse = client.submitTransfer(request);
        while(true) {

            try {
                Iterator<TransferStateApiResponse> transferStates = client.getTransferStates(TransferStateApiRequest.newBuilder().setTransferId(transferApiResponse.getTransferId()).build());
                System.out.println("Got " + transferStates.next().getState());
                TransferStateApiResponse transferState = client.getTransferState(TransferStateApiRequest.newBuilder().setTransferId(transferApiResponse.getTransferId()).build());
                System.out.println("State " + transferState.getState());
                if ("COMPLETED".equals(transferState.getState()) || "FAILED".equals(transferState.getState())) {
                    break;
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            Thread.sleep(1000);
        }
    }
}
