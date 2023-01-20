### MFT-Cli Build Instructions

Install Poetry
```
python3.10 -m venv venv  ## Use python 3.10.6 or higher
source venv/bin/activate
pip install poetry
pip install pick
```

Load Poetry shell
```
cd airavata_mft_cli
poetry shell
```

Install dependencies
```
pip install grpcio==1.46.3
pip install grpcio-tools==1.46.3
pip install airavata_mft_sdk==0.0.1-alpha21
```

Build the binary
```
poetry install
mft --help
```

To publish the ditribution to pypi
```
 poetry publish --build
```