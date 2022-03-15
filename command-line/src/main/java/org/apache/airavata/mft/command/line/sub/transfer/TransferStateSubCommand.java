package org.apache.airavata.mft.command.line.sub.transfer;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.TransferStateApiRequest;
import org.apache.airavata.mft.api.service.TransferStateApiResponse;
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
            TransferStateApiResponse transferState = mftApiClient.getTransferClient().getTransferState(
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
            Iterator<TransferStateApiResponse> transferStates = mftApiClient.getTransferClient().getTransferStates(TransferStateApiRequest.newBuilder()
                    .setMftAuthorizationToken(token).setTransferId(transferId).build());

            List<TransferStateApiResponse> states = new ArrayList<>();
            while (transferStates.hasNext()) {
                states.add(transferStates.next());
            }
            String[][] content = new String[states.size() + 1][4];
            String[] headers = {"UPDATE TIME", "STATE", "DESCRIPTION", "PERCENTAGE"};
            content[0] = headers;
            for (int i = 1; i <= states.size(); i ++) {
                TransferStateApiResponse transferState = states.get(i -1);
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
