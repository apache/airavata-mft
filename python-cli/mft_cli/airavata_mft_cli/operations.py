import typer
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.common import StorageCommon_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from rich.console import Console
from rich.table import Table
import time

def fetch_storage_and_secret_ids(storage_name):
  client = mft_client.MFTClient(transfer_api_port = 7003,
                                transfer_api_secured = False,
                                resource_service_host = "localhost",
                                resource_service_port = 7003,
                                resource_service_secured = False,
                                secret_service_host = "localhost",
                                secret_service_port = 7003)
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

  client = mft_client.MFTClient(transfer_api_port = 7003,
                                transfer_api_secured = False,
                                resource_service_host = "localhost",
                                resource_service_port = 7003,
                                resource_service_secured = False,
                                secret_service_host = "localhost",
                                secret_service_port = 7003)

  metadata_resp = client.transfer_api.resourceMetadata(resource_medata_req)
  return metadata_resp

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

def copy(source, destination):

  source_storage_id, source_secret_id = fetch_storage_and_secret_ids(source.split("/")[0])
  dest_storage_id, dest_secret_id = fetch_storage_and_secret_ids(destination.split("/")[0])

  ## TODO : Check agent availability and deploy cloud agents if required

  file_list = []
  source_metadata = get_resource_metadata(source)
  endpoint_paths = []
  total_volume = 0

  transfer_request = MFTTransferApi_pb2.TransferApiRequest(sourceStorageId = source_storage_id,
                                                           sourceSecretId = source_secret_id,
                                                           destinationStorageId = dest_storage_id,
                                                           destinationSecretId = dest_secret_id,
                                                           optimizeTransferPath = False)

  if (source_metadata.WhichOneof('metadata') == 'directory') :
    if (destination[-1] != "/"):
      print("Source is a directory path so destination path should end with /")
      raise typer.Abort()

    flatten_directories(source_metadata.directory, "", file_list)
    for file_entry in file_list:
      file = file_entry[0]
      relative_path = file_entry[1]
      endpoint_paths.append(MFTTransferApi_pb2.EndpointPaths(
          sourcePath = file.resourcePath,
          destinationPath = destination[len(destination.split("/")[0]) +1 :] + relative_path))
      total_volume += file.resourceSize

  elif (source_metadata.WhichOneof('metadata') == 'file'):
    file_list.append((source_metadata.file, source_metadata.file.friendlyName))

    if destination[-1] == "/":
      destination = destination + source_metadata.file.friendlyName

    endpoint_paths.append(MFTTransferApi_pb2.EndpointPaths(
        sourcePath = source_metadata.file.resourcePath,
        destinationPath = destination[len(destination.split("/")[0]) +1 :]))

    total_volume += source_metadata.file.resourceSize

  elif (source_metadata.WhichOneof('metadata') == 'error'):
    print("Failed while fetching source details")
    print(metadata_resp.error)
    raise typer.Abort()

  transfer_request.endpointPaths.extend(endpoint_paths)

  confirm = typer.confirm("Total number of " + str(len(endpoint_paths)) +
                          " files to be transferred. Total volume is " + str(total_volume)
                          + " bytes. Do you want to start the transfer? ", True)

  client = mft_client.MFTClient(transfer_api_port = 7003,
                                transfer_api_secured = False,
                                resource_service_host = "localhost",
                                resource_service_port = 7003,
                                resource_service_secured = False,
                                secret_service_host = "localhost",
                                secret_service_port = 7003)

  transfer_resp = client.transfer_api.submitTransfer(transfer_request)

  if not confirm:
    raise typer.Abort()

  transfer_id = transfer_resp.transferId

  state_request = MFTTransferApi_pb2.TransferStateApiRequest(transferId=transfer_id)

  ## TODO: This has to be optimized and avoid frequent polling of all transfer ids in each iteration
  ## Possible fix is to introduce a parent batch transfer id at the API level and fetch child trnasfer id
  # summaries in a single API call

  completed = 0
  failed = 0

  with typer.progressbar(length=100) as progress:

    while 1:
      state_resp = client.transfer_api.getTransferStateSummary(state_request)

      progress.update(int(state_resp.percentage * 100))
      if (state_resp.percentage == 1.0):
        completed = len(state_resp.completed)
        failed = len(state_resp.failed)
        break

      if (state_resp.state == "FAILED"):
        print("Transfer failed. Reason: " + state_resp.description)
        raise typer.Abort()
      time.sleep(1)

  print(f"Processed {completed + failed} files. Completed {completed}, Failed {failed}.")
