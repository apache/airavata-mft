import typer
import airavata_mft_cli.operations as operations
import airavata_mft_cli.bootstrap as bootstrap

app = typer.Typer()

@app.command("ls")
def list(storage_path):
  operations.list(storage_path)

@app.command("cp")
def copy(source, destination):
  operations.copy(source, destination)

@app.command("init")
def init_mft():
  bootstrap.start_mft()

@app.command("stop")
def init_mft():
  bootstrap.stop_mft()