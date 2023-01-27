#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
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

