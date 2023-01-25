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