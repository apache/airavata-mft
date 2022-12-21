import typer
from pick import pick
import mft_cli.storage.s3 as s3

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
    print("Listing storages")
