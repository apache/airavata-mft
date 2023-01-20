import typer
import airavata_mft_cli.storage
import airavata_mft_cli.base

app = airavata_mft_cli.base.app

app.add_typer(airavata_mft_cli.storage.app, name="storage")

if __name__ == "__main__":
    app()