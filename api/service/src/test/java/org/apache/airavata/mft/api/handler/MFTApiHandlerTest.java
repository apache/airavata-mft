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
        String testSourcePath = "test-file.txt";
        String testSourceStorageId = "ca0e786c-09de-40ff-a6ef-5d52a09e0027";
        String testSourceToken = "4c8337fc-f3e6-481e-b2be-ecabbd075924";
        String testDestinationPath = "test/test-file2.txt";
        String testDestinationStorageId = "e815392c-79e0-4e63-b730-7f10929b62dd";
        String testDestinationToken = "c211d1f7-e231-4147-a8dc-8af14194ee54";

        TransferApiRequest transferApiRequest = TransferApiRequest.newBuilder()
                .setSourceToken(testSourceToken)
                .setDestinationToken(testDestinationToken)
                .setDestinationStorageId(testDestinationStorageId)
                .setDestinationPath(testDestinationPath)
                .setSourceStorageId(testSourceStorageId)
                .setSourcePath(testSourcePath).build();

        String mock_transferId="mock-transfer-id-test-1234";
        when(mftConsulClient.submitTransfer(transferApiRequest)).thenReturn(mock_transferId);
        doNothing().when(mftConsulClient).saveTransferState(any(String.class), any(TransferState.class));
        StreamObserver<TransferApiResponse> observer = mock(StreamObserver.class);
        mftApiHandler.submitTransfer(transferApiRequest, observer);

        verify(observer).onNext(any(TransferApiResponse.class));
        verify(observer, times(1)).onCompleted();
    }

    @Test
    public void testInvalidSubmitTransfer() throws MFTConsulClientException {
        String testSourcePath = "test-file.txt";
        String testSourceStorageId = "ca0e786c-09de-40ff-a6ef-5d52a09e0027";
        String testSourceToken = "4c8337fc-f3e6-481e-b2be-ecabbd075924";
        String testDestinationPath = "test/test-file2.txt";
        String testDestinationStorageId = "e815392c-79e0-4e63-b730-7f10929b62dd";
        String testDestinationToken = "c211d1f7-e231-4147-a8dc-8af14194ee54";

        TransferApiRequest transferApiRequest = TransferApiRequest.newBuilder()
                .setSourceToken(testSourceToken)
                .setDestinationToken(testDestinationToken)
                .setDestinationStorageId(testDestinationStorageId)
                .setDestinationPath(testDestinationPath)
                .setSourceStorageId(testSourceStorageId)
                .setSourcePath(testSourcePath).build();

        String mock_transferId="mock-transfer-id-test-1234";
        when(mftConsulClient.submitTransfer(transferApiRequest)).thenReturn(mock_transferId);
        doNothing().when(mftConsulClient).saveTransferState(any(String.class), any(TransferState.class));
        StreamObserver<TransferApiResponse> observer = mock(StreamObserver.class);
        mftApiHandler.submitTransfer(transferApiRequest, observer);

        verify(observer).onNext(any(TransferApiResponse.class));
        verify(observer, times(1)).onCompleted();
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