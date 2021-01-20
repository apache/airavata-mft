import grpc
import MFTApi_pb2
import MFTApi_pb2_grpc

channel = grpc.insecure_channel('localhost:7004')
stub = MFTApi_pb2_grpc.MFTApiServiceStub(channel)

request = MFTApi_pb2.FetchResourceMetadataRequest(resourceId= "remote-ssh-dir-resource",
                                        resourceType = "SCP",
                                        resourceToken = "local-ssh-cred",
                                        resourceBackend = "FILE",
                                        resourceCredentialBackend= "FILE",
                                        targetAgentId = "agent0",
                                        childPath= "",
                                        mftAuthorizationToken = "user token")

response = stub.getDirectoryResourceMetadata(request)
print(response)