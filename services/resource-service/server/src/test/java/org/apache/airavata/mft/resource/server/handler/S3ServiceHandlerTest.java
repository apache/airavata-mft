package org.apache.airavata.mft.resource.server.handler;


import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageCreateRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceHandlerTest {

    @Autowired
    @InjectMocks
    private S3ServiceHandler s3ServiceHandler;

    @Mock
    private ResourceBackend backend;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void listS3StorageSuccess() throws Exception {
        // given
        S3StorageListRequest testRequest = S3StorageListRequest.newBuilder().setOffset(0).setLimit(10).build();
        // mock listS3Storage backend service response
        S3StorageListResponse testResponse = S3StorageListResponse.newBuilder().addStorages(0,
                S3Storage.newBuilder()
                        .setStorageId("test-storage-id-1234")
                        .setName("test-storage-1")
                        .setBucketName("test-bucket-1")
                        .setRegion("us-east-2")
                        .setEndpoint("https://s3.us-east-2.amazonaws.com")
                        .setUseTLS(false)).build();
        // mock stream observer
        StreamObserver<S3StorageListResponse> mockObserver = mock(StreamObserver.class);
        when(backend.listS3Storage(testRequest)).thenReturn(testResponse);

        // when
        s3ServiceHandler.listS3Storage(testRequest, mockObserver);

        //then
        verify(mockObserver, times(1)).onNext(testResponse);
        verify(mockObserver, times(1)).onCompleted();
    }

    @Test
    void listS3StorageFailure() throws Exception {
        // given
        S3StorageListRequest testRequest = S3StorageListRequest.newBuilder().setOffset(0).setLimit(10).build();
        // mock stream observer
        StreamObserver<S3StorageListResponse> mockObserver = mock(StreamObserver.class);
        // mock listS3Storage backend service response
        when(backend.listS3Storage(testRequest)).thenThrow(new Exception("Test Exception!"));

        // when
        s3ServiceHandler.listS3Storage(testRequest, mockObserver);

        //then
        verify(mockObserver, times(1)).onError(any(Throwable.class));
    }

    @Test
    void getS3Storage() {
    }

    @Test
    void successfullyCreateS3Storage() throws Exception{
        // given
        S3StorageCreateRequest testRequest = S3StorageCreateRequest.newBuilder()
                .setBucketName("test-bucket-1")
                .setRegion("us-east-2")
                .setStorageId("test-storage-id-1234")
                .setEndpoint("https://s3.us-east-2.amazonaws.com")
                .setUseTLS(false)
                .setName("test-storage-1").build();
        // mock stream observer
        StreamObserver<S3Storage> mockObserver = mock(StreamObserver.class);
        // mock createS3Storage backend service response
        S3Storage testResponse = S3Storage.newBuilder()
                .setStorageId("test-storage-id-1234")
                .setBucketName("test-bucket-1")
                .setRegion("us-east-2")
                .setEndpoint("https://s3.us-east-2.amazonaws.com")
                .setUseTLS(false)
                .setName("test-storage-1").build();
        when(backend.createS3Storage(testRequest)).thenReturn(testResponse);


        // when
        s3ServiceHandler.createS3Storage(testRequest, mockObserver);

        // then
        verify(mockObserver, times(1)).onNext(testResponse);
        verify(mockObserver, times(1)).onCompleted();
    }

    @Test
    void createS3StorageFailed() throws Exception{
        // given
        S3StorageCreateRequest testRequest = S3StorageCreateRequest.newBuilder()
                .setBucketName("test-bucket-1")
                .setRegion("us-east-2")
                .setStorageId("test-storage-id-1234")
                .setEndpoint("https://s3.us-east-2.amazonaws.com")
                .setUseTLS(false)
                .setName("test-storage-1").build();
        // mock stream observer
        StreamObserver<S3Storage> mockObserver = mock(StreamObserver.class);
        // mock createS3Storage backend service response
        when(backend.createS3Storage(testRequest)).thenThrow(new Exception("Test Error!"));

        // when
        s3ServiceHandler.createS3Storage(testRequest, mockObserver);

        // then
        verify(mockObserver, times(1)).onError(any(Throwable.class));
    }


    @Test
    void updateS3Storage() {
    }

    @Test
    void deleteS3Storage() {
    }
}