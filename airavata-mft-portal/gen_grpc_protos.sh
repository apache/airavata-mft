#!/bin/bash
# need git installed
if test ! -d "./googleapis"
then
    git clone https://github.com/googleapis/googleapis.git
fi

cd googleapis
git checkout f0b581b5bdf803e45201ecdb3688b60e381628a8
cd ..
dest="./mft_backend/resource_service"
python  -m grpc_tools.protoc -I ./googleapis -I ./../airavata-mft/services/resource-service/stub/src/main/proto/ --python_out=$dest --grpc_python_out=$dest ./../airavata-mft/services/resource-service/stub/src/main/proto/ResourceService.proto
