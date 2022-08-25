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
