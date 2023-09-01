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
import typer
from pick import pick
import airavata_mft_cli.storage.s3 as s3
import airavata_mft_cli.storage.scp as scp
import airavata_mft_cli.storage.azure as azure
import airavata_mft_cli.storage.gcs as gcs
import airavata_mft_cli.storage.local as local
import airavata_mft_cli.storage.swift as swift
import airavata_mft_cli.storage.http as http
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.common import StorageCommon_pb2
from rich.console import Console
from rich.table import Table
from rich import print
import sys
sys.path.append('../airavata_mft_cli')
from airavata_mft_cli import config as configcli
from airavata_mft_cli.util import exception_handler

app = typer.Typer(pretty_exceptions_show_locals=True)

@app.command("add")
def add_storage():
    try:
        title = "Select storage type: "
        options = ["S3", "Google Cloud Storage (GCS)", "Azure Storage", "Openstack SWIFT", "SCP", "FTP", "Box", "DropBox", "OData", "Agent", "HTTP" ]
        option, index = pick(options, title, indicator="=>")
        if option == "S3":
            s3.handle_add_storage()
        elif option == "Azure Storage":
            azure.handle_add_storage()
        elif option == "Google Cloud Storage (GCS)":
            gcs.handle_add_storage()
        elif option == "Agent":
            local.handle_add_storage()
        elif option == "Openstack SWIFT":
            swift.handle_add_storage()
        elif option == "SCP":
            scp.handle_add_storage()
        elif option == "HTTP":
            http.handle_add_storage()
    except Exception as e:
        exception_handler(e)

@app.command("remove")
def remove_storage(storage_id):
    client = mft_client.MFTClient(transfer_api_port = configcli.transfer_api_port,
                                        transfer_api_secured = configcli.transfer_api_secured,
                                        resource_service_host = configcli.resource_service_host,
                                        resource_service_port = configcli.resource_service_port,
                                        resource_service_secured = configcli.resource_service_secured,
                                        secret_service_host = configcli.secret_service_host,
                                        secret_service_port = configcli.secret_service_port)
    delete_request = StorageCommon_pb2.SecretForStorageDeleteRequest(storageId=storage_id)
    delete_response = client.common_api.deleteSecretsForStorage(delete_request)
    console = Console()
    console.print("Storage removed: " + str(delete_response.status))

@app.command("list")
def list_storage():
    try:
        client = mft_client.MFTClient(transfer_api_port = configcli.transfer_api_port,
                                    transfer_api_secured = configcli.transfer_api_secured,
                                    resource_service_host = configcli.resource_service_host,
                                    resource_service_port = configcli.resource_service_port,
                                    resource_service_secured = configcli.resource_service_secured,
                                    secret_service_host = configcli.secret_service_host,
                                    secret_service_port = configcli.secret_service_port)
        list_req = StorageCommon_pb2.StorageListRequest()
        list_response = client.common_api.listStorages(list_req)

        console = Console()
        table = Table(show_header=True, header_style='bold #2070b2')

        table.add_column('Storage Name', justify='left')
        table.add_column('Type', justify='center')
        table.add_column('Storage ID', justify='center')

        for storage in list_response.storageList:

            table.add_row('[bold]' + storage.storageName + '[/bold]',
                        StorageCommon_pb2.StorageType.Name(storage.storageType),
                        storage.storageId)

        console.print(table)
    except Exception as e:
        exception_handler(e)