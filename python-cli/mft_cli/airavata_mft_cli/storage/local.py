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
import typer
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.local import LocalStorage_pb2
import sys
sys.path.append('../airavata_mft_cli')
from airavata_mft_cli import config as configcli


def handle_add_storage():

    agent_id = typer.prompt("Agent identifier")
    name = typer.prompt("Storage name",agent_id)

    client = mft_client.MFTClient(transfer_api_port = configcli.transfer_api_port,
                                  transfer_api_secured = configcli.transfer_api_secured,
                                  resource_service_host = configcli.resource_service_host,
                                  resource_service_port = configcli.resource_service_port,
                                  resource_service_secured = configcli.resource_service_secured,
                                  secret_service_host = configcli.secret_service_host,
                                  secret_service_port = configcli.secret_service_port)

    local_storage_create_req = LocalStorage_pb2.LocalStorageCreateRequest(
        agentId = agent_id, name = name)

    created_storage = client.local_storage_api.createLocalStorage(local_storage_create_req)

    print("Successfully added the Local Bucket...")
