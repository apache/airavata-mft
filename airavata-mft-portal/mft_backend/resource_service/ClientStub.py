import grpc
import sys
import mft_backend.resource_service.ResourceService_pb2 as ResourceService_pb2
import mft_backend.resource_service.ResourceService_pb2_grpc as ResourceService_pb2_grpc


class ResourceServiceClient(object):

    def __init__(self):
        self.stub = self._connect_to_resource_server()

    @staticmethod
    def _connect_to_resource_server():
        # TODO: once TLS is enabled in server change this
        channel = grpc.insecure_channel('localhost:9093')
        try:
            grpc.channel_ready_future(channel).result(timeout=10)
        except grpc.FutureTimeoutError:
            sys.exit('Error connecting to server')
        else:
            stub = ResourceService_pb2_grpc.ResourceServiceStub(channel)
            return stub

    def get_scp_storage(self, request):
        rpc_request = ResourceService_pb2.SCPStorageGetRequest(storageId=request.storage_id)
        return self.stub.getSCPStorage(rpc_request)

    def create_scp_storage(self, request):
        rpc_request = ResourceService_pb2.SCPStorageCreateRequest(host=request.host, port=request.port)
        return self.stub.createSCPStorage(rpc_request)

    def update_scp_storage(self, request):
        rpc_request = ResourceService_pb2.SCPStorageUpdateRequest(storageId=request.storage_id, host=request.host, port=request.port)
        return self.stub.updateSCPStorage(rpc_request)

    def delete_scp_storage(self, request):
        rpc_request = ResourceService_pb2.SCPStorageDeleteRequest(storageId=request.storage_id)
        return self.stub.deleteSCPStorage(rpc_request)

    def get_scp_resource(self, request):
        rpc_request = ResourceService_pb2.SCPResourceGetRequest(resourceId=request.resource_id)
        return self.stub.getSCPResource(rpc_request)

    def create_scp_resource(self, request):
        rpc_request = ResourceService_pb2.SCPResourceCreateRequest(scpStorageId=request.scp_storage_id, resourcePath=request.resource_path)
        return self.stub.createSCPStorage(rpc_request)

    def update_scp_resource(self, request):
        rpc_request = ResourceService_pb2.SCPResourceUpdateRequest(scpStorageId=request.scp_storage_id,
                                                                   resourcePath=request.resource_path,
                                                                   resourceId=request.request_id)
        return self.stub.updateSCPResource(rpc_request)

    def delete_scp_resource(self, request):
        rpc_request = ResourceService_pb2.SCPResourceDeleteRequest(resourceId=request.request_id)
        return self.stub.deleteSCPResource(rpc_request)

# for testing
# if __name__ == "__main__":
#     client = ResourceServiceClient()
#     client.get_scp_storage("test")