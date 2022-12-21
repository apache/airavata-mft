### Generate Protobuf / Grpc sources

```
virtualenv -p python3 env
source env/bin/activate
pip install grpcio==1.46.3
pip install grpcio-tools==1.46.3
cd src/airavata_mft_sdk
./generate-stubs.sh
```

### Build the distribution

* Increase the version of the distribution in setup.cfg and run following commands 
in the directory where the setup.cfg exists. (airavata-mft/python-sdk)

* If you are not in a virtual environment created in the "Generate Protobuf / Grpc sources" section,
run following commands to create the virtual environment and install dependencies.
Otherwise, move to the next command section

```
virtualenv -p python3 env
source env/bin/activate
pip install grpcio==1.46.3
pip install grpcio-tools==1.46.3
```


```
rm -rf dist
python3 -m pip install --upgrade build
python3 -m build
python3 -m pip install --upgrade twine
python3 -m twine upload --repository pypi dist/*
```
* Commit setup.cfg file with all updates to the python stubs created in 
the src directory
