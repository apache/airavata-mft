package org.apache.airavata.mft.resource.client;

import io.grpc.ManagedChannel;
import org.apache.airavata.mft.resource.service.azure.AzureStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.box.BoxStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.dropbox.DropboxStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.ftp.FTPStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.gcs.GCSStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.local.LocalStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.s3.S3StorageServiceGrpc;
import org.apache.airavata.mft.resource.service.scp.SCPStorageServiceGrpc;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecretServiceGrpc;

import java.io.Closeable;
import java.io.IOException;

public class StorageServiceClient implements Closeable {

    private ManagedChannel channel;

    public StorageServiceClient(ManagedChannel channel) {
        this.channel = channel;
    }

    public StorageSecretServiceGrpc.StorageSecretServiceBlockingStub storageSecret() {
        return StorageSecretServiceGrpc.newBlockingStub(channel);
    }

    public SCPStorageServiceGrpc.SCPStorageServiceBlockingStub scp() {
        return SCPStorageServiceGrpc.newBlockingStub(channel);
    }

    public LocalStorageServiceGrpc.LocalStorageServiceBlockingStub local() {
        return LocalStorageServiceGrpc.newBlockingStub(channel);
    }

    public S3StorageServiceGrpc.S3StorageServiceBlockingStub s3() {
        return S3StorageServiceGrpc.newBlockingStub(channel);
    }

    public FTPStorageServiceGrpc.FTPStorageServiceBlockingStub ftp() {
        return FTPStorageServiceGrpc.newBlockingStub(channel);
    }

    public AzureStorageServiceGrpc.AzureStorageServiceBlockingStub azure() {
        return AzureStorageServiceGrpc.newBlockingStub(channel);
    }

    public GCSStorageServiceGrpc.GCSStorageServiceBlockingStub gcs() {
        return GCSStorageServiceGrpc.newBlockingStub(channel);
    }

    public BoxStorageServiceGrpc.BoxStorageServiceBlockingStub box() {
        return BoxStorageServiceGrpc.newBlockingStub(channel);
    }

    public DropboxStorageServiceGrpc.DropboxStorageServiceBlockingStub dropbox() {
        return DropboxStorageServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void close() throws IOException {

    }
}
