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
from rich import print
from pick import pick
import typer
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.scp import SCPCredential_pb2
from airavata_mft_sdk.scp import SCPStorage_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from airavata_mft_sdk import MFTAgentStubs_pb2
from airavata_mft_sdk.common import StorageCommon_pb2
import configparser
import os
import sys
sys.path.append('../airavata_mft_cli')
from airavata_mft_cli import config as configcli

def handle_add_storage():

    private_key_file = typer.prompt("Private Key File Location")

    with open(private_key_file, 'r') as file:
        private_key = file.read()

    public_key_file = typer.prompt("Public Key File Location")
    with open(public_key_file, 'r') as file:
        public_key = file.read()

    passphrase = ""
    is_pass = typer.confirm("Is there a passphrase to Private Key?", False)
    if is_pass:
        passphrase = typer.prompt("Passphrase to Private Key")
    host_name = typer.prompt("Hostname / IP")
    user_name = typer.prompt("User Name:")
    storage_name = typer.prompt("Storage Name", host_name)

    client = mft_client.MFTClient(transfer_api_port = configcli.transfer_api_port,
                                  transfer_api_secured = configcli.transfer_api_secured,
                                  resource_service_host = configcli.resource_service_host,
                                  resource_service_port = configcli.resource_service_port,
                                  resource_service_secured = configcli.resource_service_secured,
                                  secret_service_host = configcli.secret_service_host,
                                  secret_service_port = configcli.secret_service_port)

    secret_create_req = SCPCredential_pb2.SCPSecretCreateRequest(privateKey=private_key, publicKey=public_key, passphrase = passphrase, user=user_name)
    created_secret = client.scp_secret_api.createSCPSecret(secret_create_req)

    scp_storage_create_req = SCPStorage_pb2.SCPStorageCreateRequest(
        host=host_name, port=22, name=storage_name)

    created_storage = client.scp_storage_api.createSCPStorage(scp_storage_create_req)

    secret_for_storage_req = StorageCommon_pb2.SecretForStorage(storageId = created_storage.storageId,
                                       secretId = created_secret.secretId,
                                       storageType = StorageCommon_pb2.StorageType.SCP)

    client.common_api.registerSecretForStorage(secret_for_storage_req)

    print("Successfully added the SCP endpoint...")