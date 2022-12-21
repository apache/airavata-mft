import typer
import mft_cli.storage

app = typer.Typer()

storage_app = typer.Typer()
app.add_typer(mft_cli.storage.app, name="storage")


if __name__ == "__main__":
    app()