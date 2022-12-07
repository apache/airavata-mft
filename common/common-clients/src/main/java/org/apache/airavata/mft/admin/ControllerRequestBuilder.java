package org.apache.airavata.mft.admin;

import org.apache.airavata.mft.agent.stub.AgentTransferRequest;
import org.apache.airavata.mft.agent.stub.SecretWrapper;
import org.apache.airavata.mft.agent.stub.StorageWrapper;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.credential.stubs.azure.AzureSecret;
import org.apache.airavata.mft.credential.stubs.azure.AzureSecretGetRequest;
import org.apache.airavata.mft.credential.stubs.box.BoxSecret;
import org.apache.airavata.mft.credential.stubs.box.BoxSecretGetRequest;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecret;
import org.apache.airavata.mft.credential.stubs.dropbox.DropboxSecretGetRequest;
import org.apache.airavata.mft.credential.stubs.ftp.FTPSecret;
import org.apache.airavata.mft.credential.stubs.ftp.FTPSecretGetRequest;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecretGetRequest;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecret;
import org.apache.airavata.mft.credential.stubs.odata.ODataSecretGetRequest;
import org.apache.airavata.mft.credential.stubs.s3.S3Secret;
import org.apache.airavata.mft.credential.stubs.s3.S3SecretGetRequest;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecret;
import org.apache.airavata.mft.credential.stubs.scp.SCPSecretGetRequest;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecretGetRequest;
import org.apache.airavata.mft.resource.client.StorageServiceClient;
import org.apache.airavata.mft.resource.client.StorageServiceClientBuilder;
import org.apache.airavata.mft.resource.stubs.azure.storage.AzureStorage;
import org.apache.airavata.mft.resource.stubs.azure.storage.AzureStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.box.storage.BoxStorage;
import org.apache.airavata.mft.resource.stubs.box.storage.BoxStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.DropboxStorage;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.DropboxStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.ftp.storage.FTPStorage;
import org.apache.airavata.mft.resource.stubs.ftp.storage.FTPStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.local.storage.LocalStorage;
import org.apache.airavata.mft.resource.stubs.local.storage.LocalStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorage;
import org.apache.airavata.mft.resource.stubs.odata.storage.ODataStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageGetRequest;
import org.apache.airavata.mft.resource.stubs.scp.storage.SCPStorage;
import org.apache.airavata.mft.resource.stubs.scp.storage.SCPStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageTypeResolveRequest;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageTypeResolveResponse;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerRequestBuilder {

    private final Map<String, StorageWrapper> storageCache = new ConcurrentHashMap<>();
    private final Map<String, SecretWrapper> secretCache = new ConcurrentHashMap<>();

    @Autowired
    private StorageServiceClient storageServiceClient;
    
    @Autowired
    private SecretServiceClient secretServiceClient;

    public AgentTransferRequest createAgentTransferRequest(TransferApiRequest transferRequest) {
        AgentTransferRequest.Builder agentTransferBuilder = AgentTransferRequest.newBuilder();
        agentTransferBuilder.setSourcePath(transferRequest.getSourcePath());
        agentTransferBuilder.setDestinationPath(transferRequest.getDestinationPath());
        Pair<StorageWrapper, SecretWrapper> sourceCred = createCredentials(transferRequest.getSourceStorageId(),
                        transferRequest.getSourceToken());

        agentTransferBuilder.setSourceStorage(sourceCred.getLeft());
        agentTransferBuilder.setSourceSecret(sourceCred.getRight());

        Pair<StorageWrapper, SecretWrapper> destCred = createCredentials(transferRequest.getDestinationStorageId(),
                        transferRequest.getDestinationToken());

        agentTransferBuilder.setDestinationStorage(destCred.getLeft());
        agentTransferBuilder.setDestinationSecret(destCred.getRight());

        return agentTransferBuilder.build();
    }
    public Pair<StorageWrapper, SecretWrapper> createCredentials(String storageId, String secretId) {

        if (secretCache.containsKey(secretId) && storageCache.containsKey(storageId)) {
            return Pair.of(storageCache.get(storageId),secretCache.get(secretId));
        }

        StorageTypeResolveResponse resolve = storageServiceClient.common()
                .resolveStorageType(StorageTypeResolveRequest.newBuilder().setStorageId(storageId).buildPartial());
        
        StorageWrapper.Builder storageBuilder = StorageWrapper.newBuilder();
        SecretWrapper.Builder secretBuilder = SecretWrapper.newBuilder();
        
        switch (resolve.getStorageType()) {
            case "S3":
                S3Storage s3Storage = storageCache.containsKey(storageId)? storageCache.get(storageId).getS3():
                        storageServiceClient
                                .s3()
                                .getS3Storage(S3StorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setS3(s3Storage);

                S3Secret s3Secret = storageCache.containsKey(secretId)? secretCache.get(secretId).getS3():
                        secretServiceClient
                        .s3()
                        .getS3Secret(S3SecretGetRequest.newBuilder().setSecretId(secretId).build());
                
                secretBuilder.setS3(s3Secret);
                break;

            case "SCP":
                SCPStorage scpStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getScp():
                        storageServiceClient
                        .scp()
                        .getSCPStorage(SCPStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setScp(scpStorage);

                SCPSecret scpSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getScp():
                        secretServiceClient
                        .scp()
                        .getSCPSecret(SCPSecretGetRequest.newBuilder().setSecretId(secretId).build());

                secretBuilder.setScp(scpSecret);
                break;
            case "LOCAL":
                LocalStorage localStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getLocal():
                        storageServiceClient
                        .local()
                        .getLocalStorage(LocalStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setLocal(localStorage);
                break;
            case "FTP":
                FTPStorage ftpStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getFtp():
                        storageServiceClient
                        .ftp()
                        .getFTPStorage(FTPStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setFtp(ftpStorage);

                FTPSecret ftpSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getFtp():
                        secretServiceClient
                        .ftp()
                        .getFTPSecret(FTPSecretGetRequest.newBuilder().setSecretId(secretId).build());

                secretBuilder.setFtp(ftpSecret);
                break;
            case "BOX":
                BoxStorage boxStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getBox():
                        storageServiceClient
                        .box()
                        .getBoxStorage(BoxStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setBox(boxStorage);

                BoxSecret boxSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getBox():
                        secretServiceClient
                        .box()
                        .getBoxSecret(BoxSecretGetRequest.newBuilder().setSecretId(secretId).build());

                secretBuilder.setBox(boxSecret);
                break;
            case "DROPBOX":
                DropboxStorage dropboxStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getDropbox():
                        storageServiceClient
                        .dropbox()
                        .getDropboxStorage(DropboxStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setDropbox(dropboxStorage);

                DropboxSecret dropboxSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getDropbox():
                        secretServiceClient
                        .dropbox()
                        .getDropboxSecret(DropboxSecretGetRequest.newBuilder().setSecretId(secretId).build());

                secretBuilder.setDropbox(dropboxSecret);
                break;

            case "GCS":
                GCSStorage gcsStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getGcs():
                        storageServiceClient
                        .gcs()
                        .getGCSStorage(GCSStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setGcs(gcsStorage);

                GCSSecret gcsSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getGcs():
                        secretServiceClient
                        .gcs()
                        .getGCSSecret(GCSSecretGetRequest.newBuilder().setSecretId(secretId).build());

                secretBuilder.setGcs(gcsSecret);
                break;

            case "AZURE":
                AzureStorage azureStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getAzure():
                        storageServiceClient
                        .azure()
                        .getAzureStorage(AzureStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setAzure(azureStorage);

                AzureSecret azureSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getAzure():
                        secretServiceClient
                        .azure()
                        .getAzureSecret(AzureSecretGetRequest.newBuilder().setSecretId(secretId).build());

                secretBuilder.setAzure(azureSecret);
                break;

            case "SWIFT":
                SwiftStorage swiftStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getSwift():
                        storageServiceClient
                        .swift()
                        .getSwiftStorage(SwiftStorageGetRequest.newBuilder().setStorageId(storageId).build());
                storageBuilder.setSwift(swiftStorage);

                SwiftSecret swiftSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getSwift():
                        secretServiceClient
                        .swift()
                        .getSwiftSecret(SwiftSecretGetRequest.newBuilder().setSecretId(secretId).build());

                secretBuilder.setSwift(swiftSecret);
                break;

            case "ODATA":
                    ODataStorage odataStorage = storageCache.containsKey(storageId)? storageCache.get(storageId).getOdata():
                            storageServiceClient
                            .odata()
                            .getODataStorage(ODataStorageGetRequest.newBuilder().setStorageId(storageId).build());
                    storageBuilder.setOdata(odataStorage);

                    ODataSecret odataSecret = storageCache.containsKey(secretId)? secretCache.get(secretId).getOdata():
                            secretServiceClient
                            .odata()
                            .getODataSecret(ODataSecretGetRequest.newBuilder().setSecretId(secretId).build());

                    secretBuilder.setOdata(odataSecret);
                    break;
                    
        }

        if (!storageCache.containsKey(storageId)) {
            storageCache.put(storageId, storageBuilder.build());
        }

        if (!secretCache.containsKey(secretId)) {
            secretCache.put(secretId, secretBuilder.build());
        }

        return Pair.of(storageBuilder.build(), secretBuilder.build());
    }

    public SecretWrapper createSecretWrapper(String secretId) {
        SecretWrapper.Builder builder = SecretWrapper.newBuilder();

        return builder.build();
    }
}
