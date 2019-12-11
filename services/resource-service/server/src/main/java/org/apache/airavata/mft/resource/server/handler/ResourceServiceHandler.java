package org.apache.airavata.mft.resource.server.handler;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.SCPResource;
import org.apache.airavata.mft.resource.service.SCPResourceRequest;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ResourceServiceHandler extends ResourceServiceGrpc.ResourceServiceImplBase {

    @Override
    public void getSCPResource(SCPResourceRequest request, StreamObserver<SCPResource> responseObserver) {
        SCPResource.Builder resourceBuilder = SCPResource.newBuilder().setHost("localhost").setPort(22);
        responseObserver.onNext(resourceBuilder.build());
        responseObserver.onCompleted();
    }
}
