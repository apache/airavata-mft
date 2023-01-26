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
import org.apache.airavata.mft.api.service.TransferStateApiRequest;
import org.apache.airavata.mft.api.service.TransferStateResponse;
import org.apache.airavata.mft.api.service.TransferStateSummaryResponse;
import org.apache.airavata.mft.command.line.CommandLineUtil;
import org.apache.airavata.mft.common.AuthToken;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "state", description = "Returns state of a transfer")
public class TransferStateSubCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-a", "--all"}, description = "All transfer states")
    private boolean all;

    @CommandLine.Parameters(index = "0", description = "Transfer Id")
    private String transferId;

    @Override
    public Integer call() throws Exception {

        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        AuthToken token = AuthToken.newBuilder().build();
        int[] columnWidth = {15, 15, 35, 10};


        if (!all) {
            TransferStateSummaryResponse transferState = mftApiClient.getTransferClient().getTransferStateSummary(
                    TransferStateApiRequest.newBuilder()
                            .setMftAuthorizationToken(token)
                            .setTransferId(transferId).build());


            String[][] content = new String[2][4];
            String[] headers = {"UPDATE TIME", "STATE", "DESCRIPTION", "PERCENTAGE"};
            content[0] = headers;

            content[1][0] = transferState.getUpdateTimeMils() + "";
            content[1][1] = transferState.getState();
            content[1][2] = transferState.getDescription();
            content[1][3] = transferState.getPercentage() + "";

            CommandLineUtil.printTable(columnWidth, content);

        } else {
            Iterator<TransferStateResponse> transferStates = mftApiClient.getTransferClient().getAllTransferStates(TransferStateApiRequest.newBuilder()
                    .setMftAuthorizationToken(token).setTransferId(transferId).build());

            List<TransferStateResponse> states = new ArrayList<>();
            while (transferStates.hasNext()) {
                states.add(transferStates.next());
            }
            String[][] content = new String[states.size() + 1][4];
            String[] headers = {"UPDATE TIME", "STATE", "DESCRIPTION", "PERCENTAGE"};
            content[0] = headers;
            for (int i = 1; i <= states.size(); i ++) {
                TransferStateResponse transferState = states.get(i -1);
                content[i][0] = transferState.getUpdateTimeMils() + "";
                content[i][1] = transferState.getState();
                content[i][2] = transferState.getDescription();
                content[i][3] = transferState.getPercentage() + "";
            }
            CommandLineUtil.printTable(columnWidth, content);

        }
        return 0;
    }
}
