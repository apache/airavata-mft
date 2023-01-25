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

## Build and run airavata-mft as a docker container

Build the docker image from the directory containing the Dockerfile:

`docker build -t airavata/mft .`

Run the docker image:

`docker run --name mft -d airavata/mft consul`

You can access the command line:

`docker exec -it mft /bin/bash`

You can print the agent logs:

`docker logs -f mft`

## Run multiple agent on the same machine

A _docker-compose.yml_ file is available to run multiple mft agent. It will launch a consul server and _n_ mft agents.

Start the stack:

`docker-compose up -d --scale mft-agent=n`

Get the logs:

`docker-compose logs -f`

Stop every containers:

`docker-compose stop`

Stop and remove every containers:

`docker-compose down`
