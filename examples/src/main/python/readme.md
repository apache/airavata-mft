python3 -m venv venv
source venv/bin/activate
pip install --upgrade pip
pip install -r requirements.txt

mkdir -p google/api    
curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/annotations.proto > google/api/annotations.proto     
curl https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/http.proto > google/api/http.proto

python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. --proto_path=../../../../common/mft-common-proto/src/main/proto/ CredCommon.proto
python -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. --proto_path=../../../../api/stub/src/main/proto/ --proto_path=../../../../common/mft-common-proto/src/main/proto/ MFTApi.proto
