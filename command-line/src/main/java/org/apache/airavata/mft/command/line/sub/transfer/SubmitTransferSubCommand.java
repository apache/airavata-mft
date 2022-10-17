package org.apache.airavata.mft.command.line.sub.transfer;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.api.service.TransferApiResponse;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResource;
import org.apache.airavata.mft.resource.stubs.common.GenericResourceCreateRequest;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecretSearchRequest;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecretSearchResponse;
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

        StorageSecretSearchResponse sourceSecret = mftApiClient.getStorageServiceClient().storageSecret()
                .searchStorageSecret(StorageSecretSearchRequest.newBuilder()
                        .setAuthzToken(token).setStorageId(sourceStorageId).build());
        System.out.println(sourceSecret);

        StorageSecretSearchResponse destSecret = mftApiClient.getStorageServiceClient().storageSecret()
                .searchStorageSecret(StorageSecretSearchRequest.newBuilder()
                        .setAuthzToken(token).setStorageId(destinationStorageId).build());

        TransferApiResponse transferResp = mftApiClient.getTransferClient().submitTransfer(TransferApiRequest.newBuilder()
                .setSourceToken(sourceSecret.getStorageSecret().getSecretId())
                .setDestinationToken(destSecret.getStorageSecret().getSecretId())
                .setDestinationStorageId(destinationStorageId)
                .setDestinationPath(destinationPath)
                .setSourceStorageId(sourceStorageId)
                .setSourcePath(sourcePath).build());

        System.out.println("Submitted Transfer " + transferResp.getTransferId());
        return 0;
    }
}
