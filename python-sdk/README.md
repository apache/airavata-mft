<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

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
