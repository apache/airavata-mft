import grpc
import MFTApi_pb2
import MFTApi_pb2_grpc

channel = grpc.insecure_channel('localhost:7004')
stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)
download_request = MFTApi_pb2.HttpDownloadApiRequest(sourceStoreId ="remote-ssh-storage",
                                  sourcePath= "/tmp/a.txt",
                                  sourceToken = "local-ssh-cred",
                                  sourceType= "SCP",
                                  targetAgent = "agent0",
                                  mftAuthorizationToken = "")

result = stub.submitHttpDownload(download_request)
print(result)

## Sample output ##
# url: "http://localhost:3333/53937f40-d545-4180-967c-ddb193d672d8"
# targetAgent: "agent0"