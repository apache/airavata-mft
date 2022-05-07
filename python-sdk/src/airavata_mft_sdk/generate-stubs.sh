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

python3 -m grpc_tools.protoc --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          ../../../common/mft-common-proto/src/main/proto/CredCommon.proto --python_out=$COMMON_DIR --grpc_python_out=$COMMON_DIR

python3 -m grpc_tools.protoc --proto_path=../../../services/resource-service/stub/src/main/proto \
          --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          --proto_path=. \
          ../../../services/resource-service/stub/src/main/proto/resource/ResourceService.proto \
          ../../../services/resource-service/stub/src/main/proto/s3/S3StorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/s3/S3Storage.proto \
          ../../../services/resource-service/stub/src/main/proto/scp/SCPStorage.proto \
          ../../../services/resource-service/stub/src/main/proto/scp/SCPStorageService.proto \
          ../../../services/resource-service/stub/src/main/proto/resourcesecretmap/StorageSecretMap.proto \
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
          --python_out=$RESOURCE_DIR --grpc_python_out=$RESOURCE_DIR


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
          --python_out=$SECRET_DIR --grpc_python_out=$SECRET_DIR

python3 -m grpc_tools.protoc --proto_path=../../../api/stub/src/main/proto \
          --proto_path=../../../common/mft-common-proto/src/main/proto/ \
          --proto_path=. \
          ../../../api/stub/src/main/proto/MFTTransferApi.proto \
          --python_out=$TRANSFER_DIR --grpc_python_out=$TRANSFER_DIR


touch azure/__init__.py
touch box/__init__.py
touch dropbox/__init__.py
touch ftp/__init__.py
touch gcs/__init__.py
touch local/__init__.py
touch resource/__init__.py
touch resourcesecretmap/__init__.py
touch s3/__init__.py
touch scp/__init__.py
touch swift/__init__.py


sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' azure/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' box/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' dropbox/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' ftp/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' gcs/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' local/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' resource/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' resourcesecretmap/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' s3/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' scp/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' swift/*.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' *pb2.py
sed -i 's/from \([^)]*\)pb2/from airavata_mft_sdk.\1pb2/' *pb2_grpc.py

sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' azure/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' box/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' dropbox/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' ftp/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' gcs/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' local/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' resource/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' resourcesecretmap/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' s3/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' scp/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' swift/*.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' *pb2.py
sed -i 's/^import \([^)]*\)pb2/import airavata_mft_sdk.\1pb2/' *pb2_grpc.py