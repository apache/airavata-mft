package org.apache.airavata.mft.api.handler;

import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.apache.airavata.mft.admin.MFTConsulClientException;
import org.apache.airavata.mft.admin.models.TransferState;
import org.apache.airavata.mft.api.service.TransferApiRequest;
import org.apache.airavata.mft.api.service.TransferApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MFTApiHandlerTest {

    @Autowired
    @InjectMocks
    private MFTApiHandler mftApiHandler;

    @Mock
    private MFTConsulClient mftConsulClient;

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
        mftApiHandler = null;
        mftConsulClient = null;
    }

    @Test
    public void testValidSubmitTransfer() throws MFTConsulClientException {
        // given
        TransferApiRequest transferApiRequest = TransferApiRequest.newBuilder()
                .setSourceToken("mock-source-token-1234")
                .setDestinationToken("mock-destination-token-1234")
                .setDestinationStorageId("mock-destination-storage-id-1234")
                .setDestinationPath("test/test-file2.txt")
                .setSourceStorageId("mock-source-storage-id-1234")
                .setSourcePath("test-file.txt").build();
        // mftConsulClient mock behavior
        when(mftConsulClient.submitTransfer(transferApiRequest)).thenReturn("mock-transfer-id-test-1234");
        doNothing().when(mftConsulClient).saveTransferState(any(String.class), any(TransferState.class));
        // mock stream observer
        StreamObserver<TransferApiResponse> observer = mock(StreamObserver.class);

        // when
        mftApiHandler.submitTransfer(transferApiRequest, observer);

        // then
        verify(observer).onNext(any(TransferApiResponse.class));
        verify(observer, times(1)).onCompleted();
        verify(observer, times(0)).onError(any(MFTConsulClientException.class));
    }

    @Test
    public void testInvalidSubmitTransfer() throws MFTConsulClientException {
        // given
        TransferApiRequest transferApiRequest = TransferApiRequest.newBuilder()
                .setSourceToken("mock-source-token-1234")
                .setDestinationToken("mock-destination-token-1234")
                .setDestinationStorageId("mock-destination-storage-id-1234")
                .setDestinationPath("test/test-file2.txt")
                .setSourceStorageId("mock-source-storage-id-1234")
                .setSourcePath("test-file.txt").build();
        // mftConsulClient mock behavior
        when(mftConsulClient.submitTransfer(transferApiRequest)).thenThrow(new MFTConsulClientException("Test Exception"));
        // mock stream observer
        StreamObserver<TransferApiResponse> observer = mock(StreamObserver.class);

        // when
        mftApiHandler.submitTransfer(transferApiRequest, observer);

        // then
        verify(observer, times(0)).onNext(any(TransferApiResponse.class));
        verify(observer, times(0)).onCompleted();
        verify(observer, times(0)).onError(any(MFTConsulClientException.class));
    }

    @Disabled
    public void testSubmitBatchTransfer() {
    }

    @Disabled
    public void testSubmitHttpUpload() {
    }

    @Disabled
    public void testSubmitHttpDownload() {
    }

    @Disabled
    public void testGetTransferStates() {
    }

    @Disabled
    public void testGetTransferState() {
    }

    @Disabled
    public void testGetFileResourceMetadata() {
    }

    @Disabled
    public void testGetDirectoryResourceMetadata() {
    }
}