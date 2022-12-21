import grpc
import airavata_mft_sdk.MFTTransferApi_pb2_grpc as transfer_grpc
from airavata_mft_sdk.azure import AzureStorageService_pb2_grpc
from airavata_mft_sdk.box import BoxStorageService_pb2_grpc
from airavata_mft_sdk.dropbox import DropboxStorageService_pb2_grpc
from airavata_mft_sdk.ftp import FTPStorageService_pb2_grpc
from airavata_mft_sdk.gcs import GCSStorageService_pb2_grpc
from airavata_mft_sdk.local import LocalStorageService_pb2_grpc
from airavata_mft_sdk.s3 import S3StorageService_pb2_grpc
from airavata_mft_sdk.scp import SCPStorageService_pb2_grpc
from airavata_mft_sdk.common import StorageCommon_pb2_grpc


from airavata_mft_sdk.azure import AzureSecretService_pb2_grpc
from airavata_mft_sdk.box import BoxSecretService_pb2_grpc
from airavata_mft_sdk.dropbox import DropboxSecretService_pb2_grpc
from airavata_mft_sdk.ftp import FTPSecretService_pb2_grpc
from airavata_mft_sdk.gcs import GCSSecretService_pb2_grpc
from airavata_mft_sdk.s3 import S3SecretService_pb2_grpc
from airavata_mft_sdk.scp import SCPSecretService_pb2_grpc

class MFTClient:

    def __init__(self, transfer_api_host = "localhost",
                 transfer_api_port = 7004,
                 transfer_api_secured = False,
                 resource_service_host = "localhost",
                 resource_service_port = 7002,
                 resource_service_secured = False,
                 secret_service_host = "localhost",
                 secret_service_port = 7003,
                 secret_service_secured = False,):

        if (not transfer_api_secured):
            self.transfer_api_channel = grpc.insecure_channel('{}:{}'.format(transfer_api_host, transfer_api_port))
        # TODO implement secure channel
        self.transfer_api = transfer_grpc.MFTTransferServiceStub(self.transfer_api_channel)

        if (not resource_service_secured):
            self.resource_channel = grpc.insecure_channel('{}:{}'.format(resource_service_host, resource_service_port))
        # TODO implement secure channel
        self.azure_storage_api = AzureStorageService_pb2_grpc.AzureStorageServiceStub(self.resource_channel)
        self.box_storage_api = BoxStorageService_pb2_grpc.BoxStorageServiceStub(self.resource_channel)
        self.dropbox_storage_api = DropboxStorageService_pb2_grpc.DropboxStorageServiceStub(self.resource_channel)
        self.ftp_storage_api = FTPStorageService_pb2_grpc.FTPStorageServiceStub(self.resource_channel)
        self.gcs_storage_api = GCSStorageService_pb2_grpc.GCSStorageServiceStub(self.resource_channel)
        self.local_storage_api = LocalStorageService_pb2_grpc.LocalStorageServiceStub(self.resource_channel)
        self.s3_storage_api = S3StorageService_pb2_grpc.S3StorageServiceStub(self.resource_channel)
        self.scp_storage_api = SCPStorageService_pb2_grpc.SCPStorageServiceStub(self.resource_channel)
        self.common_api = StorageCommon_pb2_grpc.StorageCommonServiceStub(self.resource_channel)

        if (not secret_service_secured):
            self.secret_channel = grpc.insecure_channel('{}:{}'.format(secret_service_host, secret_service_port))
        # TODO implement secure channel
        self.azure_secret_api = AzureSecretService_pb2_grpc.AzureSecretServiceStub(self.secret_channel)
        self.box_secret_api = BoxSecretService_pb2_grpc.BoxSecretServiceStub(self.secret_channel)
        self.dropbox_secret_api = DropboxSecretService_pb2_grpc.DropboxSecretServiceStub(self.secret_channel)
        self.ftp_secret_api = FTPSecretService_pb2_grpc.FTPSecretServiceStub(self.secret_channel)
        self.gcs_secret_api = GCSSecretService_pb2_grpc.GCSSecretServiceStub(self.secret_channel)
        self.s3_secret_api = S3SecretService_pb2_grpc.S3SecretServiceStub(self.secret_channel)
        self.scp_secret_api = SCPSecretService_pb2_grpc.SCPSecretServiceStub(self.secret_channel)



