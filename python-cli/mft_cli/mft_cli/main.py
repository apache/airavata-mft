import typer
import mft_cli.storage
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.common import StorageCommon_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from rich.console import Console
from rich.table import Table
from rich.progress import track
import time

app = typer.Typer()

app.add_typer(mft_cli.storage.app, name="storage")

def fetch_storage_and_secret_ids(storage_name):
    client = mft_client.MFTClient()
    search_req = StorageCommon_pb2.StorageSearchRequest(storageName=storage_name)
    storages = client.common_api.searchStorages(search_req)

    if len(storages.storageList) == 0:
        search_req = StorageCommon_pb2.StorageSearchRequest(storageId=storage_name)
        storages = client.common_api.searchStorages(search_req)

    if len(storages.storageList) == 0:
        print("No storage with name or id " + storage_name + " was found. Please register the storage with command mft-cli storage add")
        raise typer.Abort()

    if len(storages.storageList) > 1:
        print("More than one storage with nam " + storage_name + " was found. Please use the storage id. You can fetch it from mft-cli storage list")
        raise typer.Abort()

    storage = storages.storageList[0]
    sec_req = StorageCommon_pb2.SecretForStorageGetRequest(storageId = storage.storageId)
    sec_resp = client.common_api.getSecretForStorage(sec_req)
    if sec_resp.error != 0:
        print("Could not fetch the secret for storage " + storage.storageId)

    return sec_resp.storageId, sec_resp.secretId
def get_resource_metadata(storage_path, recursive_search = False):
    storage_name = storage_path.split("/")[0]
    resource_path = storage_path[len(storage_name) +1 :]

    storage_id, secret_id = fetch_storage_and_secret_ids(storage_name)

    id_req = MFTTransferApi_pb2.GetResourceMetadataFromIDsRequest(storageId = storage_id,
                                                                  secretId = secret_id,
                                                                  resourcePath = resource_path)
    resource_medata_req = MFTTransferApi_pb2.FetchResourceMetadataRequest(idRequest = id_req)

    client = mft_client.MFTClient()

    metadata_resp = client.transfer_api.resourceMetadata(resource_medata_req)
    return metadata_resp
@app.command("ls")
def list(storage_path):

    metadata_resp = get_resource_metadata(storage_path)

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

def flatten_directories(directory, parent_path, file_list):
    for dir in directory.directories:
        flatten_directories(dir, parent_path + dir.friendlyName + "/", file_list)

    for file in directory.files:
        file_list.append((file, parent_path + file.friendlyName))

@app.command("cp")
def copy(source, destination):

    source_storage_id, source_secret_id = fetch_storage_and_secret_ids(source.split("/")[0])
    dest_storage_id, dest_secret_id = fetch_storage_and_secret_ids(destination.split("/")[0])

    ## TODO : Check agent availability and deploy cloud agents if required

    file_list = []
    source_metadata = get_resource_metadata(source)
    transfer_requests = []
    total_volume = 0

    if (source_metadata.WhichOneof('metadata') == 'directory') :
        if (destination[-1] != "/"):
            print("Source is a directory path so destination path should end with /")
            raise typer.Abort()

        flatten_directories(source_metadata.directory, "", file_list)
        for file_entry in file_list:
            file = file_entry[0]
            relative_path = file_entry[1]
            transfer_requests.append(MFTTransferApi_pb2.TransferApiRequest(
                sourcePath = file.resourcePath,
                sourceStorageId = source_storage_id,
                sourceSecretId = source_secret_id,
                destinationPath = destination[len(destination.split("/")[0]) +1 :] + relative_path,
                destinationStorageId = dest_storage_id,
                destinationSecretId = dest_secret_id))
            total_volume += file.resourceSize

    elif (source_metadata.WhichOneof('metadata') == 'file'):
        file_list.append((source_metadata.file, source_metadata.file.friendlyName))

        if destination[-1] == "/":
            destination = destination + source_metadata.file.friendlyName

        transfer_requests.append(MFTTransferApi_pb2.TransferApiRequest(
            sourcePath = source_metadata.file.resourcePath,
            sourceStorageId = source_storage_id,
            sourceSecretId = source_secret_id,
            destinationPath = destination[len(destination.split("/")[0]) +1 :],
            destinationStorageId = dest_storage_id,
            destinationSecretId = dest_secret_id))

        total_volume += source_metadata.file.resourceSize

    elif (source_metadata.WhichOneof('metadata') == 'error'):
        print("Failed while fetching source details")
        print(metadata_resp.error)
        raise typer.Abort()

    batch_transfer_request = MFTTransferApi_pb2.BatchTransferApiRequest()
    batch_transfer_request.transferRequests.extend(transfer_requests)

    confirm = typer.confirm("Total number of " + str(len(transfer_requests)) +
                        " files to be transferred. Total volume is " + str(total_volume)
                        + " bytes. Do you want to start the transfer? ", True)

    client = mft_client.MFTClient()
    batch_transfer_resp = client.transfer_api.submitBatchTransfer(batch_transfer_request)

    if not confirm:
        raise typer.Abort()

    transfer_ids = batch_transfer_resp.transferIds

    state_requests = []
    for transfer_id in transfer_ids:
        state_requests.append(MFTTransferApi_pb2.TransferStateApiRequest(transferId=transfer_id))

    ## TODO: This has to be optimized and avoid frequent polling of all transfer ids in each iteration
    ## Possible fix is to introduce a parent batch transfer id at the API level and fetch child trnasfer id
    # summaries in a single API call

    completed = 0
    failed = 0

    with typer.progressbar(length=len(transfer_ids)) as progress:

        while 1:
            completed = 0
            failed = 0
            for state_request in state_requests:
                state_resp = client.transfer_api.getTransferState(state_request)
                if state_resp.state == "COMPLETED":
                    completed += 1
                elif state_resp.state == "FAILED":
                    failed += 1

            total = completed + failed
            progress.update(total)
            if (total == len(transfer_ids)):
                break
            time.sleep(1)

    print(f"Processed {completed + failed} files. Completed {completed}, Failed {failed}.")

if __name__ == "__main__":
    app()