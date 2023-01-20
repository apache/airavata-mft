#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

from rich import print as rprint
from rich.panel import Panel
from rich.text import Text
from pick import pick
import typer
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.gcs import GCSCredential_pb2
from airavata_mft_sdk.gcs import GCSStorage_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from airavata_mft_sdk import MFTAgentStubs_pb2
from airavata_mft_sdk.common import StorageCommon_pb2
import json
import configparser
import os
import base64
import google.auth
import googleapiclient.discovery

gcs_key_path = '.mft/keys/gcs/service_account_key.json'


def handle_add_storage():

    options = ["Through Google Cloud SDK config file", "Enter manually"]
    option, index = pick(options, "How do you want to load credentials", indicator="=>")

    if index == 1:  # Manual configuration
        text_msg = ("MFT uses service accounts to gain access to Google Cloud. "
                    "\n 1) You can create a service account by going to https://console.cloud.google.com/iam-admin/serviceaccounts."
                    "\n 2) Download the JSON format of the service account key."
                    "\n 3) Use that downloaded Service Account Credential JSON file to access Google storage."
                    "\nMore information about service accounts can be found here https://cloud.google.com/iam/docs/service-accounts")

        panel = Panel.fit(Text(text_msg, justify="left"))
        rprint(panel)
        credential_json_path = typer.prompt("Service Account Credential JSON path")
        with open(credential_json_path) as json_file:
            sa_entries = json.load(json_file)
            client_id = sa_entries['client_id']
            client_email = sa_entries['client_email']
            client_secret = sa_entries['private_key']
            project_id = sa_entries['project_id']


    else:  # Loading credentials from the gcloud cli config file
        service_account = None
        client_email = None
        default_service_account_name = 'mft-gcs-serviceaccount'

        # Find the active config for Google cloud ADC
        active_config_path = os.path.join(os.path.expanduser('~'), '.config/gcloud/active_config')
        with open(active_config_path) as f:
            active_config = f.readline()
        print('Active config : ' + active_config)

        # Load default project
        config = configparser.RawConfigParser()
        default_project_path = os.path.join(os.path.expanduser('~'), '.config/gcloud/configurations/config_' + active_config)
        config.read(default_project_path)
        project_id = config['core']['project']
        account_email = config['core']['account']
        print('Project ID : ' + project_id)
        print(account_email)

        default_service_account_email = (default_service_account_name + '@' + project_id + '.iam.gserviceaccount.com')

        path = os.path.join(os.path.expanduser('~'), gcs_key_path)
        if os.path.exists(path):
            with open(path, 'r') as config_file:
                config_data = json.load(config_file)
            client_id = config_data['client_id']
            client_email = config_data['client_email']
            project_id = config_data['project_id']
            client_secret = config_data['private_key']

        print('Client email : ', client_email)
        if client_email is None or client_email != default_service_account_email:
            inferred_cred, inferred_project = google.auth.default(active_config)

            service_acc_list = list_service_accounts(project_id=project_id, credentials=inferred_cred)
            service_account_available = any(x['email'] == default_service_account_email for x in service_acc_list['accounts'])
            print('Service account availability : ', service_account_available)
            if service_account_available:  # Already have a service account for mft, But no key
                get_service_account_key(credentials=inferred_cred, service_account_email=default_service_account_email)

            else:  # No service account for mft

                # Read OAuth credentials and create the service account
                service_account = create_service_account(project_id, default_service_account_name, 'Airavata MFT Service Account', credentials=inferred_cred)

            # Load service account details again
            if os.path.exists(path):
                with open(path, 'r') as config_file:
                    config_data = json.load(config_file)
                client_id = config_data['client_id']
                client_email = config_data['client_email']
                project_id = config_data['project_id']
                client_secret = config_data['private_key']
            else:
                print("No credential found in ~/" + gcs_key_path + " file")
                exit()

    client = mft_client.MFTClient(transfer_api_port = 7003,
                                  transfer_api_secured = False,
                                  resource_service_host = "localhost",
                                  resource_service_port = 7003,
                                  resource_service_secured = False,
                                  secret_service_host = "localhost",
                                  secret_service_port = 7003)

    gcs_secret = GCSCredential_pb2.GCSSecret(clientEmail=client_email, privateKey=client_secret, projectId=project_id)
    secret_wrapper = MFTAgentStubs_pb2.SecretWrapper(gcs=gcs_secret)

    gcs_storage = GCSStorage_pb2.GCSStorage()
    storage_wrapper = MFTAgentStubs_pb2.StorageWrapper(gcs=gcs_storage)

    direct_req = MFTAgentStubs_pb2.GetResourceMetadataRequest(resourcePath="", secret=secret_wrapper,
                                                              storage=storage_wrapper)
    resource_medata_req = MFTTransferApi_pb2.FetchResourceMetadataRequest(directRequest=direct_req)
    metadata_resp = client.transfer_api.resourceMetadata(resource_medata_req)

    bucket_options = ["Manually Enter"]

    bucket_list = metadata_resp.directory.directories
    if len(bucket_list) > 0:
        for b in bucket_list:
            bucket_options.append(b.friendlyName)

    title = "Select the Bucket: "
    selected_bucket, index = pick(bucket_options, title, indicator="=>")
    if index == 0:
        selected_bucket = typer.prompt("Enter bucket name ")
    storage_name = typer.prompt("Name of the storage ", selected_bucket)

    gcs_storage_create_req = GCSStorage_pb2.GCSStorageCreateRequest(storageId=storage_name, bucketName=selected_bucket,
                                                                    name=storage_name)
    created_storage = client.gcs_storage_api.createGCSStorage(gcs_storage_create_req)

    secret_create_req = GCSCredential_pb2.GCSSecretCreateRequest(clientEmail=client_id, privateKey=client_secret,
                                                                 projectId=project_id)
    created_secret = client.gcs_secret_api.createGCSSecret(secret_create_req)

    secret_for_storage_req = StorageCommon_pb2.SecretForStorage(storageId=created_storage.storageId,
                                                                secretId=created_secret.secretId,
                                                                storageType=StorageCommon_pb2.StorageType.GCS)
    client.common_api.registerSecretForStorage(secret_for_storage_req)

    print("Successfully added the GCS Bucket...")


def list_service_accounts(project_id, credentials):
    """Lists all service accounts for the current project."""

    service = googleapiclient.discovery.build('iam', 'v1', credentials=credentials)
    service_accounts = service.projects().serviceAccounts().list(name='projects/' + project_id).execute()
    # for account in service_accounts['accounts']:
    #     print('Name: ' + account['name'])
    #     print('Email: ' + account['email'])
    #     print(' ')
    return service_accounts


def create_service_account(project_id, name, display_name, credentials):
    """Creates a service account."""

    service = googleapiclient.discovery.build('iam', 'v1', credentials=credentials)
    my_service_account = service.projects().serviceAccounts().create(
        name='projects/' + project_id,
        body={
            'accountId': name,
            'serviceAccount': {
                'displayName': display_name
            }
        }).execute()

    print('Created service account: ' + my_service_account['email'])

    resource_service = googleapiclient.discovery.build('cloudresourcemanager', 'v1', credentials=credentials)
    policy = resource_service.projects().getIamPolicy(resource=project_id).execute()
    account_handle = f"serviceAccount:{my_service_account['email']}"

    # Add policy
    modified = False
    roles = [role["role"] for role in policy["bindings"]]
    target_role = "roles/storage.admin"  # Service account role which can transfer GCS files
    if target_role not in roles:
        for role in policy["bindings"]:
            if role["role"] == target_role:
                if account_handle not in role["members"]:
                    role["members"].append(account_handle)
                    modified = True

    else:  # role does not exist
        policy["bindings"].append({"role": target_role, "members": [account_handle]})
        modified = True

    if modified:  # execute policy change
        resource_service.projects().setIamPolicy(resource=project_id, body={"policy": policy}).execute()

    # Generate a Service account key
    get_service_account_key(credentials, my_service_account['email'])
    return my_service_account


def get_service_account_key(credentials, service_account_email):
    """Creates a key for a service account."""

    key_path = os.path.join(os.path.expanduser('~'), gcs_key_path)
    service = googleapiclient.discovery.build('iam', 'v1', credentials=credentials)
    # write key file
    if not os.path.exists(key_path):

        # list existing keys
        keys = service.projects().serviceAccounts().keys().list(name="projects/-/serviceAccounts/" + service_account_email).execute()

        # cannot have more than 10 keys per service account
        if len(keys["keys"]) >= 10:
            raise ValueError(f"Service account {service_account_email} has too many keys. Make sure to copy keys to {key_path} or create a new service account.")

        # create key
        key = (service.projects().serviceAccounts().keys().create(name="projects/-/serviceAccounts/" + service_account_email, body={}).execute())
        print("New Key generated ...")
        # create service key files
        os.makedirs(os.path.dirname(key_path), exist_ok=True)
        json_key_file = base64.b64decode(key["privateKeyData"]).decode("utf-8")
        open(key_path, "w").write(json_key_file)

    else:
        print("Please backup the existing in service_account_key file ~/" + gcs_key_path + " to create new key file")
        exit()

    return key_path
