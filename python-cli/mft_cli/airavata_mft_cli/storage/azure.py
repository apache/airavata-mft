from rich import print
from pick import pick
import typer
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.azure import AzureCredential_pb2
from airavata_mft_sdk.azure import AzureStorage_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from airavata_mft_sdk import MFTAgentStubs_pb2
from airavata_mft_sdk.common import StorageCommon_pb2

def handle_add_storage():

    options = ["Through Azure Cli config file", "Enter manually" ]
    option, index = pick(options, "How do you want to load credentials", indicator="=>")

    if index == 1: # Manual configuration
        connection_string = typer.prompt("Connection String")

    client = mft_client.MFTClient(transfer_api_port = 7003,
                                  transfer_api_secured = False,
                                  resource_service_host = "localhost",
                                  resource_service_port = 7003,
                                  resource_service_secured = False,
                                  secret_service_host = "localhost",
                                  secret_service_port = 7003)

    azure_secret = AzureCredential_pb2.AzureSecret(connectionString = connection_string)
    secret_wrapper = MFTAgentStubs_pb2.SecretWrapper(azure=azure_secret)

    azure_storage = AzureStorage_pb2.AzureStorage()
    storage_wrapper = MFTAgentStubs_pb2.StorageWrapper(azure=azure_storage)

    direct_req = MFTAgentStubs_pb2.GetResourceMetadataRequest(resourcePath="", secret=secret_wrapper, storage=storage_wrapper)
    resource_medata_req = MFTTransferApi_pb2.FetchResourceMetadataRequest(directRequest = direct_req)
    metadata_resp = client.transfer_api.resourceMetadata(resource_medata_req)

    container_options = ["Manually Enter"]

    container_list = metadata_resp.directory.directories
    if len(container_list) > 0:
        for c in container_list:
            container_options.append(c.friendlyName)

    title = "Select the Container: "
    selected_container, index = pick(container_options, title, indicator="=>")

    if index == 0:
        selected_container = typer.prompt("Enter container name ")
    storage_name = typer.prompt("Name of the storage ", selected_container)

    azure_storage_create_req = AzureStorage_pb2.AzureStorageCreateRequest(container= selected_container, name =storage_name)

    created_storage = client.azure_storage_api.createAzureStorage(azure_storage_create_req)

    secret_create_req= AzureCredential_pb2.AzureSecretCreateRequest(connectionString = connection_string)
    created_secret = client.azure_secret_api.createAzureSecret(secret_create_req)

    secret_for_storage_req = StorageCommon_pb2.SecretForStorage(storageId = created_storage.storageId,
                                                                secretId = created_secret.secretId,
                                                                storageType = StorageCommon_pb2.StorageType.AZURE)

    client.common_api.registerSecretForStorage(secret_for_storage_req)

    print("Successfully added the Azure Bucket...")

