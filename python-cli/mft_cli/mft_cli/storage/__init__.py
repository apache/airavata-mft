import typer
from pick import pick
import mft_cli.storage.s3 as s3
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


@app.command("list")
def list_storage():
    client = mft_client.MFTClient()
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