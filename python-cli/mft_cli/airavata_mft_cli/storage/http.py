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
from airavata_mft_sdk.http import HttpCredential_pb2
from airavata_mft_sdk.http import HTTPStorage_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from airavata_mft_sdk import MFTAgentStubs_pb2
from airavata_mft_sdk.common import StorageCommon_pb2
import configparser
import os
import sys
sys.path.append('../airavata_mft_cli')
from airavata_mft_cli import config as configcli

def handle_add_storage():

    client = mft_client.MFTClient(transfer_api_port = configcli.transfer_api_port,
                                      transfer_api_secured = configcli.transfer_api_secured,
                                      resource_service_host = configcli.resource_service_host,
                                      resource_service_port = configcli.resource_service_port,
                                      resource_service_secured = configcli.resource_service_secured,
                                      secret_service_host = configcli.secret_service_host,
                                      secret_service_port = configcli.secret_service_port)

    base_url = typer.prompt("Base URL")
    storage_name = typer.prompt("Storage Name", base_url)

    options = ["Basic Auth", "Token" ]
    option, index = pick(options, "What is the authentication method", indicator="=>")

    if index == 0:
        user_name = typer.prompt("User Name")
        password = typer.prompt("Password")
        basic_auth = HttpCredential_pb2.BasicAuth(userName=user_name, password=password)
        http_secret = HttpCredential_pb2.HTTPSecret(basic=basic_auth)
    elif index == 1:
        token = typer.prompt("Token")
        token_auth = HttpCredential_pb2.TokenAuth(accessToken=token)
        http_secret = HttpCredential_pb2.HTTPSecret(token=token_auth)

    secret_create_req= HttpCredential_pb2.HTTPSecretCreateRequest(secret=http_secret)
    created_secret = client.http_secret_api.createHTTPSecret(secret_create_req)

    http_storage_create_req = HTTPStorage_pb2.HTTPStorageCreateRequest(
            baseUrl=base_url, name=storage_name)

    created_storage = client.http_storage_api.createHTTPStorage(http_storage_create_req)

    secret_for_storage_req = StorageCommon_pb2.SecretForStorage(storageId = created_storage.storageId,
                                           secretId = created_secret.secretId,
                                           storageType = StorageCommon_pb2.StorageType.HTTP)

    client.common_api.registerSecretForStorage(secret_for_storage_req)

    print("Successfully added the HTTP endpoint...")



