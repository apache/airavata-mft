package org.apache.airavata.mft.resource.server.handler;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestResourceServiceHandler {

    @Test
    public void testGetScpStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageGetRequest scpStorageGetRequest = Mockito.mock(SCPStorageGetRequest.class);
        StreamObserver<SCPStorage> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        SCPStorage scpStorage = Mockito.mock(SCPStorage.class);

        try {
            Mockito.when(resourceBackend.getSCPStorage(scpStorageGetRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getSCPStorage(scpStorageGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetScpStorage_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageGetRequest scpStorageGetRequest = Mockito.mock(SCPStorageGetRequest.class);
        StreamObserver<SCPStorage> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getSCPStorage(scpStorageGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getSCPStorage(scpStorageGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(SCPStorage.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetScpStorage_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageGetRequest scpStorageGetRequest = Mockito.mock(SCPStorageGetRequest.class);
        StreamObserver<SCPStorage> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getSCPStorage(scpStorageGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getSCPStorage(scpStorageGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(SCPStorage.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateScpStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageCreateRequest scpStorageCreateRequest = Mockito.mock(SCPStorageCreateRequest.class);
        StreamObserver<SCPStorage> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        SCPStorage scpStorage = Mockito.mock(SCPStorage.class);

        try {
            Mockito.when(resourceBackend.createSCPStorage(scpStorageCreateRequest)).thenReturn(scpStorage);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createSCPStorage(scpStorageCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateScpStorage_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageCreateRequest scpStorageCreateRequest = Mockito.mock(SCPStorageCreateRequest.class);
        StreamObserver<SCPStorage> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        SCPStorage scpStorage = Mockito.mock(SCPStorage.class);

        try {
            Mockito.when(resourceBackend.createSCPStorage(scpStorageCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createSCPStorage(scpStorageCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateScpStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageUpdateRequest scpStorageUpdateRequest = Mockito.mock(SCPStorageUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateSCPStorage(scpStorageUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateSCPStorage(scpStorageUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateSCPStorage(scpStorageUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateScpStorage_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageUpdateRequest scpStorageUpdateRequest = Mockito.mock(SCPStorageUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateSCPStorage(scpStorageUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateSCPStorage(scpStorageUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateSCPStorage(scpStorageUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpStorageSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageDeleteRequest scpStorageDeleteRequest = Mockito.mock(SCPStorageDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteSCPStorage(scpStorageDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteSCPStorage(scpStorageDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteSCPStorage(scpStorageDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpStorageFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageDeleteRequest scpStorageDeleteRequest = Mockito.mock(SCPStorageDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteSCPStorage(scpStorageDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteSCPStorage(scpStorageDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteSCPStorage(scpStorageDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpStorage_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPStorageDeleteRequest scpStorageDeleteRequest = Mockito.mock(SCPStorageDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteSCPStorage(scpStorageDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteSCPStorage(scpStorageDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteSCPStorage(scpStorageDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetScpResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceGetRequest scpResourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        StreamObserver<SCPResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        SCPResource scpStorage = Mockito.mock(SCPResource.class);

        try {
            Mockito.when(resourceBackend.getSCPResource(scpResourceGetRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getSCPResource(scpResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetScpResource_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceGetRequest scpResourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        StreamObserver<SCPResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getSCPResource(scpResourceGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getSCPResource(scpResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(SCPResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetScpResource_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceGetRequest scpResourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        StreamObserver<SCPResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getSCPResource(scpResourceGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getSCPResource(scpResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(SCPResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateScpResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceCreateRequest scpResourceCreateRequest = Mockito.mock(SCPResourceCreateRequest.class);
        StreamObserver<SCPResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        SCPResource scpResource = Mockito.mock(SCPResource.class);

        try {
            Mockito.when(resourceBackend.createSCPResource(scpResourceCreateRequest)).thenReturn(scpResource);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createSCPResource(scpResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpResource);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateScpResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceCreateRequest scpResourceCreateRequest = Mockito.mock(SCPResourceCreateRequest.class);
        StreamObserver<SCPResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        SCPResource scpResource = Mockito.mock(SCPResource.class);

        try {
            Mockito.when(resourceBackend.createSCPResource(scpResourceCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createSCPResource(scpResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(scpResource);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateScpResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceUpdateRequest scpResourceUpdateRequest = Mockito.mock(SCPResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateSCPResource(scpResourceUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateSCPResource(scpResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateSCPResource(scpResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateScpResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceUpdateRequest scpResourceUpdateRequest = Mockito.mock(SCPResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateSCPResource(scpResourceUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateSCPResource(scpResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateSCPResource(scpResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpResourceSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceDeleteRequest scpResourceDeleteRequest = Mockito.mock(SCPResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteSCPResource(scpResourceDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteSCPResource(scpResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteSCPResource(scpResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpResourceFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceDeleteRequest scpResourceDeleteRequest = Mockito.mock(SCPResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteSCPResource(scpResourceDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteSCPResource(scpResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteSCPResource(scpResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        SCPResourceDeleteRequest scpResourceDeleteRequest = Mockito.mock(SCPResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteSCPResource(scpResourceDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteSCPResource(scpResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteSCPResource(scpResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetLocalResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceGetRequest resourceRequest = Mockito.mock(LocalResourceGetRequest.class);
        StreamObserver<LocalResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        LocalResource scpStorage = Mockito.mock(LocalResource.class);

        try {
            Mockito.when(resourceBackend.getLocalResource(resourceRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getLocalResource(resourceRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetLocalResource_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceGetRequest LocalResourceGetRequest = Mockito.mock(LocalResourceGetRequest.class);
        StreamObserver<LocalResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getLocalResource(LocalResourceGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getLocalResource(LocalResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(LocalResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetLocalResource_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceGetRequest LocalResourceGetRequest = Mockito.mock(LocalResourceGetRequest.class);
        StreamObserver<LocalResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getLocalResource(LocalResourceGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getLocalResource(LocalResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(LocalResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateLocalResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceCreateRequest LocalResourceCreateRequest = Mockito.mock(LocalResourceCreateRequest.class);
        StreamObserver<LocalResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        LocalResource LocalResource = Mockito.mock(LocalResource.class);

        try {
            Mockito.when(resourceBackend.createLocalResource(LocalResourceCreateRequest)).thenReturn(LocalResource);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createLocalResource(LocalResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(LocalResource);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateLocalResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceCreateRequest LocalResourceCreateRequest = Mockito.mock(LocalResourceCreateRequest.class);
        StreamObserver<LocalResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        LocalResource LocalResource = Mockito.mock(LocalResource.class);

        try {
            Mockito.when(resourceBackend.createLocalResource(LocalResourceCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createLocalResource(LocalResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(LocalResource);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateLocalResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceUpdateRequest LocalResourceUpdateRequest = Mockito.mock(LocalResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateLocalResource(LocalResourceUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateLocalResource(LocalResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateLocalResource(LocalResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateLocalResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceUpdateRequest LocalResourceUpdateRequest = Mockito.mock(LocalResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateLocalResource(LocalResourceUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateLocalResource(LocalResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateLocalResource(LocalResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteLocalResourceSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceDeleteRequest LocalResourceDeleteRequest = Mockito.mock(LocalResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteLocalResource(LocalResourceDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteLocalResource(LocalResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteLocalResource(LocalResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteLocalResourceFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceDeleteRequest LocalResourceDeleteRequest = Mockito.mock(LocalResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteLocalResource(LocalResourceDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteLocalResource(LocalResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteLocalResource(LocalResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(Exception.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteLocalResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        LocalResourceDeleteRequest LocalResourceDeleteRequest = Mockito.mock(LocalResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteLocalResource(LocalResourceDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteLocalResource(LocalResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteLocalResource(LocalResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetS3Resource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceGetRequest resourceRequest = Mockito.mock(S3ResourceGetRequest.class);
        StreamObserver<S3Resource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        S3Resource scpStorage = Mockito.mock(S3Resource.class);

        try {
            Mockito.when(resourceBackend.getS3Resource(resourceRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getS3Resource(resourceRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetS3Resource_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceGetRequest S3ResourceGetRequest = Mockito.mock(S3ResourceGetRequest.class);
        StreamObserver<S3Resource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getS3Resource(S3ResourceGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getS3Resource(S3ResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(S3Resource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetS3Resource_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceGetRequest S3ResourceGetRequest = Mockito.mock(S3ResourceGetRequest.class);
        StreamObserver<S3Resource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getS3Resource(S3ResourceGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getS3Resource(S3ResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(S3Resource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateS3Resource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceCreateRequest S3ResourceCreateRequest = Mockito.mock(S3ResourceCreateRequest.class);
        StreamObserver<S3Resource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        S3Resource S3Resource = Mockito.mock(S3Resource.class);

        try {
            Mockito.when(resourceBackend.createS3Resource(S3ResourceCreateRequest)).thenReturn(S3Resource);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createS3Resource(S3ResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(S3Resource);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateS3Resource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceCreateRequest S3ResourceCreateRequest = Mockito.mock(S3ResourceCreateRequest.class);
        StreamObserver<S3Resource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        S3Resource S3Resource = Mockito.mock(S3Resource.class);

        try {
            Mockito.when(resourceBackend.createS3Resource(S3ResourceCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createS3Resource(S3ResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(S3Resource);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateS3Resource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceUpdateRequest S3ResourceUpdateRequest = Mockito.mock(S3ResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateS3Resource(S3ResourceUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateS3Resource(S3ResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateS3Resource(S3ResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateS3Resource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceUpdateRequest S3ResourceUpdateRequest = Mockito.mock(S3ResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateS3Resource(S3ResourceUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateS3Resource(S3ResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateS3Resource(S3ResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteS3ResourceSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceDeleteRequest S3ResourceDeleteRequest = Mockito.mock(S3ResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteS3Resource(S3ResourceDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteS3Resource(S3ResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteS3Resource(S3ResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteS3ResourceFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceDeleteRequest S3ResourceDeleteRequest = Mockito.mock(S3ResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteS3Resource(S3ResourceDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteS3Resource(S3ResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteS3Resource(S3ResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(Exception.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteS3Resource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        S3ResourceDeleteRequest S3ResourceDeleteRequest = Mockito.mock(S3ResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteS3Resource(S3ResourceDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteS3Resource(S3ResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteS3Resource(S3ResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetBoxResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceGetRequest resourceRequest = Mockito.mock(BoxResourceGetRequest.class);
        StreamObserver<BoxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        BoxResource scpStorage = Mockito.mock(BoxResource.class);

        try {
            Mockito.when(resourceBackend.getBoxResource(resourceRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getBoxResource(resourceRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetBoxResource_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceGetRequest BoxResourceGetRequest = Mockito.mock(BoxResourceGetRequest.class);
        StreamObserver<BoxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getBoxResource(BoxResourceGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getBoxResource(BoxResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(BoxResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetBoxResource_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceGetRequest BoxResourceGetRequest = Mockito.mock(BoxResourceGetRequest.class);
        StreamObserver<BoxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getBoxResource(BoxResourceGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getBoxResource(BoxResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(BoxResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateBoxResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceCreateRequest BoxResourceCreateRequest = Mockito.mock(BoxResourceCreateRequest.class);
        StreamObserver<BoxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        BoxResource BoxResource = Mockito.mock(BoxResource.class);

        try {
            Mockito.when(resourceBackend.createBoxResource(BoxResourceCreateRequest)).thenReturn(BoxResource);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createBoxResource(BoxResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(BoxResource);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateBoxResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceCreateRequest BoxResourceCreateRequest = Mockito.mock(BoxResourceCreateRequest.class);
        StreamObserver<BoxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        BoxResource BoxResource = Mockito.mock(BoxResource.class);

        try {
            Mockito.when(resourceBackend.createBoxResource(BoxResourceCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createBoxResource(BoxResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(BoxResource);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateBoxResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceUpdateRequest BoxResourceUpdateRequest = Mockito.mock(BoxResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateBoxResource(BoxResourceUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateBoxResource(BoxResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateBoxResource(BoxResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateBoxResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceUpdateRequest BoxResourceUpdateRequest = Mockito.mock(BoxResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateBoxResource(BoxResourceUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateBoxResource(BoxResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateBoxResource(BoxResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteBoxResourceSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceDeleteRequest BoxResourceDeleteRequest = Mockito.mock(BoxResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteBoxResource(BoxResourceDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteBoxResource(BoxResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteBoxResource(BoxResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteBoxResourceFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceDeleteRequest BoxResourceDeleteRequest = Mockito.mock(BoxResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteBoxResource(BoxResourceDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteBoxResource(BoxResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteBoxResource(BoxResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(Exception.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteBoxResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        BoxResourceDeleteRequest BoxResourceDeleteRequest = Mockito.mock(BoxResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteBoxResource(BoxResourceDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteBoxResource(BoxResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteBoxResource(BoxResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetAzureResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceGetRequest resourceRequest = Mockito.mock(AzureResourceGetRequest.class);
        StreamObserver<AzureResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        AzureResource scpStorage = Mockito.mock(AzureResource.class);

        try {
            Mockito.when(resourceBackend.getAzureResource(resourceRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getAzureResource(resourceRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetAzureResource_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceGetRequest AzureResourceGetRequest = Mockito.mock(AzureResourceGetRequest.class);
        StreamObserver<AzureResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getAzureResource(AzureResourceGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getAzureResource(AzureResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(AzureResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetAzureResource_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceGetRequest AzureResourceGetRequest = Mockito.mock(AzureResourceGetRequest.class);
        StreamObserver<AzureResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getAzureResource(AzureResourceGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getAzureResource(AzureResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(AzureResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateAzureResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceCreateRequest AzureResourceCreateRequest = Mockito.mock(AzureResourceCreateRequest.class);
        StreamObserver<AzureResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        AzureResource AzureResource = Mockito.mock(AzureResource.class);

        try {
            Mockito.when(resourceBackend.createAzureResource(AzureResourceCreateRequest)).thenReturn(AzureResource);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createAzureResource(AzureResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(AzureResource);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateAzureResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceCreateRequest AzureResourceCreateRequest = Mockito.mock(AzureResourceCreateRequest.class);
        StreamObserver<AzureResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        AzureResource AzureResource = Mockito.mock(AzureResource.class);

        try {
            Mockito.when(resourceBackend.createAzureResource(AzureResourceCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createAzureResource(AzureResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(AzureResource);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateAzureResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceUpdateRequest AzureResourceUpdateRequest = Mockito.mock(AzureResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateAzureResource(AzureResourceUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateAzureResource(AzureResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateAzureResource(AzureResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateAzureResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceUpdateRequest AzureResourceUpdateRequest = Mockito.mock(AzureResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateAzureResource(AzureResourceUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateAzureResource(AzureResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateAzureResource(AzureResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteAzureResourceSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceDeleteRequest AzureResourceDeleteRequest = Mockito.mock(AzureResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteAzureResource(AzureResourceDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteAzureResource(AzureResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteAzureResource(AzureResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteAzureResourceFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceDeleteRequest AzureResourceDeleteRequest = Mockito.mock(AzureResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteAzureResource(AzureResourceDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteAzureResource(AzureResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteAzureResource(AzureResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(Exception.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteAzureResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        AzureResourceDeleteRequest AzureResourceDeleteRequest = Mockito.mock(AzureResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteAzureResource(AzureResourceDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteAzureResource(AzureResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteAzureResource(AzureResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetGCSResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceGetRequest resourceRequest = Mockito.mock(GCSResourceGetRequest.class);
        StreamObserver<GCSResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        GCSResource scpStorage = Mockito.mock(GCSResource.class);

        try {
            Mockito.when(resourceBackend.getGCSResource(resourceRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getGCSResource(resourceRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetGCSResource_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceGetRequest GCSResourceGetRequest = Mockito.mock(GCSResourceGetRequest.class);
        StreamObserver<GCSResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getGCSResource(GCSResourceGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getGCSResource(GCSResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(GCSResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetGCSResource_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceGetRequest GCSResourceGetRequest = Mockito.mock(GCSResourceGetRequest.class);
        StreamObserver<GCSResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getGCSResource(GCSResourceGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getGCSResource(GCSResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(GCSResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateGCSResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceCreateRequest GCSResourceCreateRequest = Mockito.mock(GCSResourceCreateRequest.class);
        StreamObserver<GCSResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        GCSResource GCSResource = Mockito.mock(GCSResource.class);

        try {
            Mockito.when(resourceBackend.createGCSResource(GCSResourceCreateRequest)).thenReturn(GCSResource);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createGCSResource(GCSResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(GCSResource);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateGCSResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceCreateRequest GCSResourceCreateRequest = Mockito.mock(GCSResourceCreateRequest.class);
        StreamObserver<GCSResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        GCSResource GCSResource = Mockito.mock(GCSResource.class);

        try {
            Mockito.when(resourceBackend.createGCSResource(GCSResourceCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createGCSResource(GCSResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(GCSResource);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateGCSResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceUpdateRequest GCSResourceUpdateRequest = Mockito.mock(GCSResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateGCSResource(GCSResourceUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateGCSResource(GCSResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateGCSResource(GCSResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateGCSResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceUpdateRequest GCSResourceUpdateRequest = Mockito.mock(GCSResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateGCSResource(GCSResourceUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateGCSResource(GCSResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateGCSResource(GCSResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteGCSResourceSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceDeleteRequest GCSResourceDeleteRequest = Mockito.mock(GCSResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteGCSResource(GCSResourceDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteGCSResource(GCSResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteGCSResource(GCSResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteGCSResourceFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceDeleteRequest GCSResourceDeleteRequest = Mockito.mock(GCSResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteGCSResource(GCSResourceDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteGCSResource(GCSResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteGCSResource(GCSResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(Exception.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteGCSResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        GCSResourceDeleteRequest GCSResourceDeleteRequest = Mockito.mock(GCSResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteGCSResource(GCSResourceDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteGCSResource(GCSResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteGCSResource(GCSResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetDropboxResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceGetRequest resourceRequest = Mockito.mock(DropboxResourceGetRequest.class);
        StreamObserver<DropboxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        DropboxResource scpStorage = Mockito.mock(DropboxResource.class);

        try {
            Mockito.when(resourceBackend.getDropboxResource(resourceRequest)).thenReturn(Optional.of(scpStorage));
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getDropboxResource(resourceRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpStorage);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetDropboxResource_EmptyStorage() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceGetRequest DropboxResourceGetRequest = Mockito.mock(DropboxResourceGetRequest.class);
        StreamObserver<DropboxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getDropboxResource(DropboxResourceGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getDropboxResource(DropboxResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(DropboxResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetDropboxResource_SCPStorageThrowsError() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceGetRequest DropboxResourceGetRequest = Mockito.mock(DropboxResourceGetRequest.class);
        StreamObserver<DropboxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.getDropboxResource(DropboxResourceGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.getDropboxResource(DropboxResourceGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(DropboxResource.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateDropboxResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceCreateRequest DropboxResourceCreateRequest = Mockito.mock(DropboxResourceCreateRequest.class);
        StreamObserver<DropboxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        DropboxResource DropboxResource = Mockito.mock(DropboxResource.class);

        try {
            Mockito.when(resourceBackend.createDropboxResource(DropboxResourceCreateRequest)).thenReturn(DropboxResource);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createDropboxResource(DropboxResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(DropboxResource);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testCreateDropboxResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceCreateRequest DropboxResourceCreateRequest = Mockito.mock(DropboxResourceCreateRequest.class);
        StreamObserver<DropboxResource> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);
        DropboxResource DropboxResource = Mockito.mock(DropboxResource.class);

        try {
            Mockito.when(resourceBackend.createDropboxResource(DropboxResourceCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        resourceServiceHandler.createDropboxResource(DropboxResourceCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(DropboxResource);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateDropboxResource() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceUpdateRequest DropboxResourceUpdateRequest = Mockito.mock(DropboxResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateDropboxResource(DropboxResourceUpdateRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateDropboxResource(DropboxResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateDropboxResource(DropboxResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateDropboxResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceUpdateRequest DropboxResourceUpdateRequest = Mockito.mock(DropboxResourceUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.updateDropboxResource(DropboxResourceUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.updateDropboxResource(DropboxResourceUpdateRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).updateDropboxResource(DropboxResourceUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteDropboxResourceSuccessful() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceDeleteRequest DropboxResourceDeleteRequest = Mockito.mock(DropboxResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteDropboxResource(DropboxResourceDeleteRequest)).thenReturn(true);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteDropboxResource(DropboxResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteDropboxResource(DropboxResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteDropboxResourceFail() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceDeleteRequest DropboxResourceDeleteRequest = Mockito.mock(DropboxResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteDropboxResource(DropboxResourceDeleteRequest)).thenReturn(false);
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteDropboxResource(DropboxResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteDropboxResource(DropboxResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(Exception.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteDropboxResource_CreateThrowsException() {
        ResourceServiceHandler resourceServiceHandler = Mockito.spy(new ResourceServiceHandler());
        DropboxResourceDeleteRequest DropboxResourceDeleteRequest = Mockito.mock(DropboxResourceDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        ResourceBackend resourceBackend = Mockito.mock(ResourceBackend.class);

        try {
            Mockito.when(resourceBackend.deleteDropboxResource(DropboxResourceDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(resourceBackend).when(resourceServiceHandler).getBackend();

            resourceServiceHandler.deleteDropboxResource(DropboxResourceDeleteRequest, streamObserver);

            Mockito.verify(resourceBackend, Mockito.times(1)).deleteDropboxResource(DropboxResourceDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }
}
