from rich import print
from pick import pick
import typer
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.gcs import GCSCredential_pb2
from airavata_mft_sdk.gcs import GCSStorage_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from airavata_mft_sdk import MFTAgentStubs_pb2
from airavata_mft_sdk.common import StorageCommon_pb2
import json

import os


def handle_add_storage():
    session_token = ""
    gcs_regions = ["us-central", "us-east1", "us-east4", "us-east5", "us-south1",
                   "us-west1", "us-west2", "us-west3", "us-west4", "northamerica-northeast1", "northamerica-northeast2",
                   "southamerica-east1", "southamerica-west1",
                   "europe-central2", "europe-north1", "europe-southwest1", "europe-west1", "europe-west2",
                   "europe-west3", "europe-west4", "europe-west6", "europe-west8", "europe-west9",
                   "asia-east1", "asia-east2", "asia-northeast1", "asia-northeast2", "asia-northeast3",
                   "asia-southeast1", "asia-south1", "asia-south2", "asia-southeast2",
                   "me-west1", "australia-southeast1", "australia-southeast2"
                   ]

    options = ["Through Google Cloud SDK config file", "Enter manually"]
    option, index = pick(options, "How do you want to load credentials", indicator="=>")

    if index == 1:  # Manual configuration
        client_id = typer.prompt("Client ID")
        client_secret = typer.prompt("Secret Key")
        has_session_token = typer.confirm("Do you have a session token?", False)
        if has_session_token:
            session_token = typer.prompt("Session Token")

        is_gcs = typer.confirm("Is this a Google Cloud bucket?", True)

        if is_gcs:
            region, index = pick(gcp_regions, "Select the Google Cloud Region", indicator="=>")

    else:  # Loading credentials from the aws cli config file
        path = os.path.join(os.path.expanduser('~'), '.config/gcloud/application_default_credentials.json')
        with open(path, 'r') as config_file:
            config_data = json.load(config_file)
        cred_sections = "1"

        # TODO Need to create service account and load the credential from it
        client_id = config_data['client_id']
        client_id = 'xx@mfttest.iam.gserviceaccount.com'
        # client_secret = base64.b64decode(config_data['client_secret']).decode("utf-8")
        client_secret = 'privetKey'
        project_id = 'mfttest'

        region, index = pick(gcs_regions, "Select the Google Cloud Region", indicator="=>")

    client = mft_client.MFTClient()

    gcs_secret = GCSCredential_pb2.GCSSecret(clientEmail=client_id, privateKey=client_secret, projectId=project_id)
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
