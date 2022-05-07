from airavata_mft_sdk import mft_client
from airavata_mft_sdk import MFTTransferApi_pb2
from airavata_mft_sdk.s3 import S3Storage_pb2

client = mft_client.MFTClient()



create_request = S3Storage_pb2.S3StorageCreateRequest(bucketName = "bucket",
                                                           region = "us-east",
                                                           storageId = "some_id",
                                                           endpoint = "https://endpoint.url",
                                                           name = "s3-storage")

print(client.s3_storage_api.createS3Storage(create_request))

transfer_request = MFTTransferApi_pb2.TransferApiRequest(sourceResourceId="source_id",
                                                         sourceType = "S3",
                                                         sourceToken = "source_token",
                                                         destinationResourceId = "dest_id",
                                                         destinationType = "S3",
                                                         destinationToken = "dest_token")
print(client.transfer_api.submitTransfer(transfer_request))

