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
from airavata_mft_sdk.swift import SwiftCredential_pb2
from airavata_mft_sdk.swift import SwiftStorage_pb2
from airavata_mft_sdk.common import StorageCommon_pb2
import sys
sys.path.append('../airavata_mft_cli')
from airavata_mft_cli import config as configcli

def handle_add_storage():

    options = ["v3", "v2" ]
    option, index = pick(options, "Select Keystone Auth Version", indicator="=>")

    secret_create_req= SwiftCredential_pb2.SwiftSecretCreateRequest()
    if (option == "v3"):

        user_name = typer.prompt("User Name")
        password = typer.prompt("Password")
        tenant_name = typer.prompt("Project Name")
        project_domain = typer.prompt("Project Domain Name", "Default")
        user_domain = typer.prompt("User Domain Name", "Default")
        v3_sec = SwiftCredential_pb2.SwiftV3AuthSecret(userDomainName=user_domain, userName=user_name,
                                              password=password, tenantName=tenant_name, projectDomainName=project_domain)
        secret_create_req.v3AuthSecret.CopyFrom(v3_sec)
    else :
        tenant_name = typer.prompt("Tenant Name")
        user_name = typer.prompt("User Name")
        password = typer.prompt("Password")
        v2_sec = SwiftCredential_pb2.SwiftV2AuthSecret(tenant=tenant_name, userName=user_name, password=password)
        secret_create_req.v2AuthSecret.CopyFrom(v2_sec)


    auth_url = typer.prompt("Auth URL")
    secret_create_req.endpoint = auth_url

    region_name = typer.prompt("Region Name")
    container = typer.prompt("Container")

    storage_name = typer.prompt("Name of the storage ", container)

    client = mft_client.MFTClient(transfer_api_port = configcli.transfer_api_port,
                                  transfer_api_secured = configcli.transfer_api_secured,
                                  resource_service_host = configcli.resource_service_host,
                                  resource_service_port = configcli.resource_service_port,
                                  resource_service_secured = configcli.resource_service_secured,
                                  secret_service_host = configcli.secret_service_host,
                                  secret_service_port = configcli.secret_service_port)


    swift_storage_create_req = SwiftStorage_pb2.SwiftStorageCreateRequest(region=region_name,
                                                                          container=container,
                                                                          name=storage_name)

    created_storage = client.swift_storage_api.createSwiftStorage(swift_storage_create_req)

    created_secret = client.swift_secret_api.createSwiftSecret(secret_create_req)

    secret_for_storage_req = StorageCommon_pb2.SecretForStorage(storageId = created_storage.storageId,
                                                                secretId = created_secret.secretId,
                                                                storageType = StorageCommon_pb2.StorageType.SWIFT)

    client.common_api.registerSecretForStorage(secret_for_storage_req)

    print("Successfully added the Swift Container...")