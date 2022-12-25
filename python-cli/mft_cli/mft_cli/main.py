import typer
import mft_cli.storage
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.common import StorageCommon_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from rich.console import Console
from rich.table import Table

app = typer.Typer()

app.add_typer(mft_cli.storage.app, name="storage")

@app.command("ls")
def list(storage_path):
    storage_name = storage_path.split("/")[0]
    resource_path = storage_path[len(storage_name) +1 :]
    client = mft_client.MFTClient()
    search_req = StorageCommon_pb2.StorageSearchRequest(storageName=storage_name)
    storages = client.common_api.searchStorages(search_req)

    if len(storages.storageList) == 0:
        search_req = StorageCommon_pb2.StorageSearchRequest(storageId=storage_name)
        storages = client.common_api.searchStorages(search_req)

    if len(storages.storageList) == 0:
        print("No storage with name or id " + storage_name + " was found. Please register the storage with command mft-cli storage add")
        exit()

    if len(storages.storageList) > 1:
        print("More than one storage with nam " + storage_name + " was found. Please use the storage id. You can fetch it from mft-cli storage list")
        exit()

    storage = storages.storageList[0]
    sec_req = StorageCommon_pb2.SecretForStorageGetRequest(storageId = storage.storageId)
    sec_resp = client.common_api.getSecretForStorage(sec_req)
    if sec_resp.error != 0:
        print("Could not fetch the secret for storage " + storage.storageId)

    id_req = MFTTransferApi_pb2.GetResourceMetadataFromIDsRequest(storageId = sec_resp.storageId,
                                                                        secretId = sec_resp.secretId,
                                                                        resourcePath = resource_path)
    resource_medata_req = MFTTransferApi_pb2.FetchResourceMetadataRequest(idRequest = id_req)

    metadata_resp = client.transfer_api.resourceMetadata(resource_medata_req)

    console = Console()
    table = Table()

    table.add_column('Name', justify='left')
    table.add_column('Type', justify='center')
    table.add_column('Size', justify='center')

    if (metadata_resp.WhichOneof('metadata') == 'directory') :
        for dir in metadata_resp.directory.directories:
            table.add_row('[bold]' + dir.friendlyName + '[/bold]', 'DIR', '')

        for file in metadata_resp.directory.files:
            table.add_row('[bold]' + file.friendlyName + '[/bold]', 'FILE', str(file.resourceSize))

    elif (metadata_resp.WhichOneof('metadata') == 'file'):
        table.add_row('[bold]' + metadata_resp.file.friendlyName + '[/bold]', 'FILE', str(metadata_resp.file.resourceSize))

    elif (metadata_resp.WhichOneof('metadata') == 'error'):
        print(metadata_resp.error)

    console.print(table)

@app.command("cp")
def copy(source, destination):
    print("Moving data from " + source + " to " + destination)


if __name__ == "__main__":
    app()