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
import airavata_mft_cli.storage.azure as azure
import airavata_mft_cli.storage.gcs as gcs
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.common import StorageCommon_pb2
from rich.console import Console
from rich.table import Table

app = typer.Typer()

@app.command("add")
def add_storage():
    title = "Select storage type: "
    options = ["S3", "Google Cloud Storage (GCS)", "Azure Storage", "Openstack SWIFT", "SCP", "FTP", "Box", "DropBox", "OData", "Agent" ]
    option, index = pick(options, title, indicator="=>")
    if option == "S3":
        s3.handle_add_storage()
    elif option == "Azure Storage":
        azure.handle_add_storage()
    elif option == "Google Cloud Storage (GCS)":
        gcs.handle_add_storage()

@app.command("list")
def list_storage():
    client = mft_client.MFTClient(transfer_api_port = 7003,
                                  transfer_api_secured = False,
                                  resource_service_host = "localhost",
                                  resource_service_port = 7003,
                                  resource_service_secured = False,
                                  secret_service_host = "localhost",
                                  secret_service_port = 7003)
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