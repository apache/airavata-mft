### Generate Protobuf / Grpc sources

```
virtualenv -p python3 env
source env/bin/activate
pip install grpcio grpcio-tools
cd src/airavata_mft_sdk
./generate-stubs.sh
```

### Build the distribution
```
python3 -m pip install --upgrade build
python3 -m build
python3 -m pip install --upgrade twine
python3 -m twine upload --repository pypi dist/*
```

