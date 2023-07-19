# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

RESOURCE_DIR=.
SECRET_DIR=.
COMMON_DIR=.
TRANSFER_DIR=.

mkdir -p $RESOURCE_DIR
mkdir -p $SECRET_DIR
mkdir -p $COMMON_DIR
mkdir -p $TRANSFER_DIR

touch $RESOURCE_DIR/__init__.py
touch $SECRET_DIR/__init__.py
touch $COMMON_DIR/__init__.py
touch $COMMON_DIR/__init__.py

echo "Building Common Stubs........."
python3 -m grpc_tools.protoc --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          ../../../common/mft-common-proto/src/main/proto/CredCommon.proto --python_out=$COMMON_DIR --grpc_python_out=$COMMON_DIR

echo "Building Resource Stubs........."
python3 -m grpc_tools.protoc --proto_path=../../../services/resource-service/stub/src/main/proto \
          --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          --proto_path=. \
          ../../../services/resource-service/stub/src/main/proto/s3/S3StorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/s3/S3Storage.proto \
          ../../../services/resource-service/stub/src/main/proto/scp/SCPStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/scp/SCPStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/local/LocalStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/local/LocalStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/gcs/GCSStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/gcs/GCSStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/ftp/FTPStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/ftp/FTPStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/dropbox/DropboxStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/dropbox/DropboxStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/box/BoxStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/box/BoxStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/azure/AzureStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/azure/AzureStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/swift/SwiftStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/swift/SwiftStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/odata/ODataStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/odata/ODataStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/http/HTTPStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/http/HTTPStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/common/StorageCommon.proto \
          --python_out=$RESOURCE_DIR --grpc_python_out=$RESOURCE_DIR

echo "Building Secret Stubs........."
python3 -m grpc_tools.protoc --proto_path=../../../services/secret-service/stub/src/main/proto \
          --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          --proto_path=. \
          ../../../services/secret-service/stub/src/main/proto/azure/AzureCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/azure/AzureSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/box/BoxCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/box/BoxSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/dropbox/DropboxCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/dropbox/DropboxSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/ftp/FTPCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/ftp/FTPSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/gcs/GCSCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/gcs/GCSSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/s3/S3Credential.proto \
          ../../../services/secret-service/stub/src/main/proto/s3/S3SecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/scp/SCPCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/scp/SCPSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/swift/SwiftCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/swift/SwiftSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/odata/ODataCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/odata/ODataSecretService.proto \
          ../../../services/secret-service/stub/src/main/proto/http/HttpCredential.proto \
          ../../../services/secret-service/stub/src/main/proto/http/HttpSecretService.proto \
          --python_out=$SECRET_DIR --grpc_python_out=$SECRET_DIR

echo "Building Agent Stubs........."
python3 -m grpc_tools.protoc --proto_path=../../../agent/stub/src/main/proto/ \
          --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          --proto_path=../../../services/resource-service/stub/src/main/proto \
          --proto_path=../../../services/secret-service/stub/src/main/proto \
          --proto_path=. \
          ../../../agent/stub/src/main/proto/MFTAgentStubs.proto --python_out=. --grpc_python_out=.

echo "Building API Stubs........."

python3 -m grpc_tools.protoc --proto_path=../../../api/stub/src/main/proto \
          --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          --proto_path=../../../services/resource-service/stub/src/main/proto \
          --proto_path=../../../services/secret-service/stub/src/main/proto \
          --proto_path=../../../agent/stub/src/main/proto/ \
          --proto_path=. \
          ../../../api/stub/src/main/proto/MFTTransferApi.proto \
          --python_out=$TRANSFER_DIR --grpc_python_out=$TRANSFER_DIR


touch azure/__init__.py
touch box/__init__.py
touch dropbox/__init__.py
touch ftp/__init__.py
touch gcs/__init__.py
touch local/__init__.py
touch s3/__init__.py
touch scp/__init__.py
touch swift/__init__.py
touch odata/__init__.py
touch http/__init__.py
touch common/__init__.py


sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' azure/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' box/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' dropbox/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' ftp/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' gcs/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' local/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' s3/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' scp/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' swift/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' odata/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' http/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' common/*.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' *pb2.py
sed -i'.bak' -e 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' *pb2_grpc.py

sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' azure/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' box/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' dropbox/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' ftp/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' gcs/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' local/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' s3/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' scp/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' swift/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' odata/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' http/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' common/*.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' *pb2.py
sed -i'.bak' -e 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' *pb2_grpc.py

find . -name "*.bak" -type f -delete