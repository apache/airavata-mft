package org.apache.airavata.mft.secret.server.handler;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.apache.airavata.mft.secret.service.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

public class TestSecretServiceHandler {

    @Test
    public void testGetScpSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        SCPSecretGetRequest scpSecretGetRequest = Mockito.mock(SCPSecretGetRequest.class);
        StreamObserver<SCPSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        SCPSecret scpSecret = Mockito.mock(SCPSecret.class);

        try {
            Mockito.when(secretBackend.getSCPSecret(scpSecretGetRequest)).thenReturn(Optional.of(scpSecret));
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getSCPSecret(scpSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpSecret);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetScpSecret_EmptySecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        SCPSecretGetRequest scpSecretGetRequest = Mockito.mock(SCPSecretGetRequest.class);
        StreamObserver<SCPSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getSCPSecret(scpSecretGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getSCPSecret(scpSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(SCPSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetScpSecret_SCPSecretThrowsError() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        SCPSecretGetRequest scpSecretGetRequest = Mockito.mock(SCPSecretGetRequest.class);
        StreamObserver<SCPSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getSCPSecret(scpSecretGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getSCPSecret(scpSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(SCPSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateScpSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        SCPSecretCreateRequest scpSecretCreateRequest = Mockito.mock(SCPSecretCreateRequest.class);
        StreamObserver<SCPSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        SCPSecret scpSecret = Mockito.mock(SCPSecret.class);

        try {
            Mockito.when(secretBackend.createSCPSecret(scpSecretCreateRequest)).thenReturn(scpSecret);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.createSCPSecret(scpSecretCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpSecret);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testUpdateScpSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        SCPSecretUpdateRequest scpSecretUpdateRequest = Mockito.mock(SCPSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateSCPSecret(scpSecretUpdateRequest)).thenReturn(false);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateSCPSecret(scpSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateSCPSecret(scpSecretUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpSecretSuccessful() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        SCPSecretDeleteRequest scpSecretDeleteRequest = Mockito.mock(SCPSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteSCPSecret(scpSecretDeleteRequest)).thenReturn(true);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteSCPSecret(scpSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteSCPSecret(scpSecretDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteScpSecretFail() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        SCPSecretDeleteRequest scpSecretDeleteRequest = Mockito.mock(SCPSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteSCPSecret(scpSecretDeleteRequest)).thenReturn(false);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteSCPSecret(scpSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteSCPSecret(scpSecretDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetS3Secret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretGetRequest secretRequest = Mockito.mock(S3SecretGetRequest.class);
        StreamObserver<S3Secret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        S3Secret scpSecret = Mockito.mock(S3Secret.class);

        try {
            Mockito.when(secretBackend.getS3Secret(secretRequest)).thenReturn(Optional.of(scpSecret));
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getS3Secret(secretRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpSecret);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetS3Secret_EmptySecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretGetRequest s3SecretGetRequest = Mockito.mock(S3SecretGetRequest.class);
        StreamObserver<S3Secret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getS3Secret(s3SecretGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getS3Secret(s3SecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(S3Secret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetS3Secret_SCPSecretThrowsError() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretGetRequest s3SecretGetRequest = Mockito.mock(S3SecretGetRequest.class);
        StreamObserver<S3Secret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getS3Secret(s3SecretGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getS3Secret(s3SecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(S3Secret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateS3Secret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretCreateRequest s3SecretCreateRequest = Mockito.mock(S3SecretCreateRequest.class);
        StreamObserver<S3Secret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        S3Secret S3Secret = Mockito.mock(S3Secret.class);

        try {
            Mockito.when(secretBackend.createS3Secret(s3SecretCreateRequest)).thenReturn(S3Secret);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.createS3Secret(s3SecretCreateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).createS3Secret(s3SecretCreateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testCreateS3Secret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretCreateRequest s3SecretCreateRequest = Mockito.mock(S3SecretCreateRequest.class);
        StreamObserver<S3Secret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        S3Secret S3Secret = Mockito.mock(S3Secret.class);

        try {
            Mockito.when(secretBackend.createS3Secret(s3SecretCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.createS3Secret(s3SecretCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(S3Secret);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateS3Secret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretUpdateRequest s3SecretUpdateRequest = Mockito.mock(S3SecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateS3Secret(s3SecretUpdateRequest)).thenReturn(false);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateS3Secret(s3SecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateS3Secret(s3SecretUpdateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateS3Secret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretUpdateRequest s3SecretUpdateRequest = Mockito.mock(S3SecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateS3Secret(s3SecretUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateS3Secret(s3SecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateS3Secret(s3SecretUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteS3SecretSuccessful() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretDeleteRequest s3SecretDeleteRequest = Mockito.mock(S3SecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteS3Secret(s3SecretDeleteRequest)).thenReturn(true);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteS3Secret(s3SecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteS3Secret(s3SecretDeleteRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteS3Secret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        S3SecretDeleteRequest s3SecretDeleteRequest = Mockito.mock(S3SecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteS3Secret(s3SecretDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteS3Secret(s3SecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteS3Secret(s3SecretDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetBoxSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretGetRequest secretRequest = Mockito.mock(BoxSecretGetRequest.class);
        StreamObserver<BoxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        BoxSecret scpSecret = Mockito.mock(BoxSecret.class);

        try {
            Mockito.when(secretBackend.getBoxSecret(secretRequest)).thenReturn(Optional.of(scpSecret));
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getBoxSecret(secretRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpSecret);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetBoxSecret_EmptySecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretGetRequest boxSecretGetRequest = Mockito.mock(BoxSecretGetRequest.class);
        StreamObserver<BoxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getBoxSecret(boxSecretGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getBoxSecret(boxSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(BoxSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(2)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetBoxSecret_SCPSecretThrowsError() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretGetRequest boxSecretGetRequest = Mockito.mock(BoxSecretGetRequest.class);
        StreamObserver<BoxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getBoxSecret(boxSecretGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getBoxSecret(boxSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(BoxSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(2)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateBoxSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretCreateRequest boxSecretCreateRequest = Mockito.mock(BoxSecretCreateRequest.class);
        StreamObserver<BoxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        BoxSecret BoxSecret = Mockito.mock(BoxSecret.class);

        try {
            Mockito.when(secretBackend.createBoxSecret(boxSecretCreateRequest)).thenReturn(BoxSecret);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.createBoxSecret(boxSecretCreateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).createBoxSecret(boxSecretCreateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testCreateBoxSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretCreateRequest boxSecretCreateRequest = Mockito.mock(BoxSecretCreateRequest.class);
        StreamObserver<BoxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        BoxSecret BoxSecret = Mockito.mock(BoxSecret.class);

        try {
            Mockito.when(secretBackend.createBoxSecret(boxSecretCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.createBoxSecret(boxSecretCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(BoxSecret);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateBoxSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretUpdateRequest boxSecretUpdateRequest = Mockito.mock(BoxSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateBoxSecret(boxSecretUpdateRequest)).thenReturn(false);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateBoxSecret(boxSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateBoxSecret(boxSecretUpdateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateBoxSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretUpdateRequest boxSecretUpdateRequest = Mockito.mock(BoxSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateBoxSecret(boxSecretUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateBoxSecret(boxSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateBoxSecret(boxSecretUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteBoxSecretSuccessful() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretDeleteRequest boxSecretDeleteRequest = Mockito.mock(BoxSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteBoxSecret(boxSecretDeleteRequest)).thenReturn(true);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteBoxSecret(boxSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteBoxSecret(boxSecretDeleteRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteBoxSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        BoxSecretDeleteRequest boxSecretDeleteRequest = Mockito.mock(BoxSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteBoxSecret(boxSecretDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteBoxSecret(boxSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteBoxSecret(boxSecretDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetAzureSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretGetRequest secretRequest = Mockito.mock(AzureSecretGetRequest.class);
        StreamObserver<AzureSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        AzureSecret scpSecret = Mockito.mock(AzureSecret.class);

        try {
            Mockito.when(secretBackend.getAzureSecret(secretRequest)).thenReturn(Optional.of(scpSecret));
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getAzureSecret(secretRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpSecret);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetAzureSecret_EmptySecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretGetRequest azureSecretGetRequest = Mockito.mock(AzureSecretGetRequest.class);
        StreamObserver<AzureSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getAzureSecret(azureSecretGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getAzureSecret(azureSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(AzureSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(2)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetAzureSecret_SCPSecretThrowsError() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretGetRequest azureSecretGetRequest = Mockito.mock(AzureSecretGetRequest.class);
        StreamObserver<AzureSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getAzureSecret(azureSecretGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getAzureSecret(azureSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(AzureSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(2)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateAzureSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretCreateRequest azureSecretCreateRequest = Mockito.mock(AzureSecretCreateRequest.class);
        StreamObserver<AzureSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        AzureSecret AzureSecret = Mockito.mock(AzureSecret.class);

        try {
            Mockito.when(secretBackend.createAzureSecret(azureSecretCreateRequest)).thenReturn(AzureSecret);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.createAzureSecret(azureSecretCreateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).createAzureSecret(azureSecretCreateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testCreateAzureSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretCreateRequest azureSecretCreateRequest = Mockito.mock(AzureSecretCreateRequest.class);
        StreamObserver<AzureSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        AzureSecret AzureSecret = Mockito.mock(AzureSecret.class);

        try {
            Mockito.when(secretBackend.createAzureSecret(azureSecretCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.createAzureSecret(azureSecretCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(AzureSecret);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateAzureSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretUpdateRequest azureSecretUpdateRequest = Mockito.mock(AzureSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateAzureSecret(azureSecretUpdateRequest)).thenReturn(false);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateAzureSecret(azureSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateAzureSecret(azureSecretUpdateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateAzureSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretUpdateRequest azureSecretUpdateRequest = Mockito.mock(AzureSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateAzureSecret(azureSecretUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateAzureSecret(azureSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateAzureSecret(azureSecretUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteAzureSecretSuccessful() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretDeleteRequest azureSecretDeleteRequest = Mockito.mock(AzureSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteAzureSecret(azureSecretDeleteRequest)).thenReturn(true);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteAzureSecret(azureSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteAzureSecret(azureSecretDeleteRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteAzureSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        AzureSecretDeleteRequest azureSecretDeleteRequest = Mockito.mock(AzureSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteAzureSecret(azureSecretDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteAzureSecret(azureSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteAzureSecret(azureSecretDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetGCSSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretGetRequest secretRequest = Mockito.mock(GCSSecretGetRequest.class);
        StreamObserver<GCSSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        GCSSecret scpSecret = Mockito.mock(GCSSecret.class);

        try {
            Mockito.when(secretBackend.getGCSSecret(secretRequest)).thenReturn(Optional.of(scpSecret));
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getGCSSecret(secretRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpSecret);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetGCSSecret_EmptySecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretGetRequest gcsSecretGetRequest = Mockito.mock(GCSSecretGetRequest.class);
        StreamObserver<GCSSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getGCSSecret(gcsSecretGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getGCSSecret(gcsSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(GCSSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetGCSSecret_SCPSecretThrowsError() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretGetRequest gcsSecretGetRequest = Mockito.mock(GCSSecretGetRequest.class);
        StreamObserver<GCSSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getGCSSecret(gcsSecretGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getGCSSecret(gcsSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(GCSSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateGCSSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretCreateRequest gcsSecretCreateRequest = Mockito.mock(GCSSecretCreateRequest.class);
        StreamObserver<GCSSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        GCSSecret GCSSecret = Mockito.mock(GCSSecret.class);

        try {
            Mockito.when(secretBackend.createGCSSecret(gcsSecretCreateRequest)).thenReturn(GCSSecret);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.createGCSSecret(gcsSecretCreateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).createGCSSecret(gcsSecretCreateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testCreateGCSSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretCreateRequest gcsSecretCreateRequest = Mockito.mock(GCSSecretCreateRequest.class);
        StreamObserver<GCSSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        GCSSecret GCSSecret = Mockito.mock(GCSSecret.class);

        try {
            Mockito.when(secretBackend.createGCSSecret(gcsSecretCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.createGCSSecret(gcsSecretCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(GCSSecret);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateGCSSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretUpdateRequest gcsSecretUpdateRequest = Mockito.mock(GCSSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateGCSSecret(gcsSecretUpdateRequest)).thenReturn(false);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateGCSSecret(gcsSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateGCSSecret(gcsSecretUpdateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateGCSSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretUpdateRequest gcsSecretUpdateRequest = Mockito.mock(GCSSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateGCSSecret(gcsSecretUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateGCSSecret(gcsSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateGCSSecret(gcsSecretUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteGCSSecretSuccessful() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretDeleteRequest gcsSecretDeleteRequest = Mockito.mock(GCSSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteGCSSecret(gcsSecretDeleteRequest)).thenReturn(true);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteGCSSecret(gcsSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteGCSSecret(gcsSecretDeleteRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteGCSSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        GCSSecretDeleteRequest gcsSecretDeleteRequest = Mockito.mock(GCSSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteGCSSecret(gcsSecretDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteGCSSecret(gcsSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteGCSSecret(gcsSecretDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testGetDropboxSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretGetRequest secretRequest = Mockito.mock(DropboxSecretGetRequest.class);
        StreamObserver<DropboxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        DropboxSecret scpSecret = Mockito.mock(DropboxSecret.class);

        try {
            Mockito.when(secretBackend.getDropboxSecret(secretRequest)).thenReturn(Optional.of(scpSecret));
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getDropboxSecret(secretRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(1)).onNext(scpSecret);
        Mockito.verify(streamObserver, Mockito.times(1)).onCompleted();
    }

    @Test
    public void testGetDropboxSecret_EmptySecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretGetRequest dropboxSecretGetRequest = Mockito.mock(DropboxSecretGetRequest.class);
        StreamObserver<DropboxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getDropboxSecret(dropboxSecretGetRequest)).thenReturn(Optional.empty());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getDropboxSecret(dropboxSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(DropboxSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testGetDropboxSecret_SCPSecretThrowsError() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretGetRequest dropboxSecretGetRequest = Mockito.mock(DropboxSecretGetRequest.class);
        StreamObserver<DropboxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.getDropboxSecret(dropboxSecretGetRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.getDropboxSecret(dropboxSecretGetRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(Mockito.any(DropboxSecret.class));
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testCreateDropboxSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretCreateRequest dropboxSecretCreateRequest = Mockito.mock(DropboxSecretCreateRequest.class);
        StreamObserver<DropboxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        DropboxSecret DropboxSecret = Mockito.mock(DropboxSecret.class);

        try {
            Mockito.when(secretBackend.createDropboxSecret(dropboxSecretCreateRequest)).thenReturn(DropboxSecret);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.createDropboxSecret(dropboxSecretCreateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).createDropboxSecret(dropboxSecretCreateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testCreateDropboxSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretCreateRequest dropboxSecretCreateRequest = Mockito.mock(DropboxSecretCreateRequest.class);
        StreamObserver<DropboxSecret> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);
        DropboxSecret DropboxSecret = Mockito.mock(DropboxSecret.class);

        try {
            Mockito.when(secretBackend.createDropboxSecret(dropboxSecretCreateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();
        } catch (Exception e) {
            fail(e);
        }

        secretServiceHandler.createDropboxSecret(dropboxSecretCreateRequest, streamObserver);

        Mockito.verify(streamObserver, Mockito.times(0)).onNext(DropboxSecret);
        Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
        Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
    }

    @Test
    public void testUpdateDropboxSecret() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretUpdateRequest dropboxSecretUpdateRequest = Mockito.mock(DropboxSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateDropboxSecret(dropboxSecretUpdateRequest)).thenReturn(false);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateDropboxSecret(dropboxSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateDropboxSecret(dropboxSecretUpdateRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testUpdateDropboxSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretUpdateRequest dropboxSecretUpdateRequest = Mockito.mock(DropboxSecretUpdateRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.updateDropboxSecret(dropboxSecretUpdateRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.updateDropboxSecret(dropboxSecretUpdateRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).updateDropboxSecret(dropboxSecretUpdateRequest);
            Mockito.verify(streamObserver, Mockito.times(0)).onCompleted();
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteDropboxSecretSuccessful() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretDeleteRequest dropboxSecretDeleteRequest = Mockito.mock(DropboxSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteDropboxSecret(dropboxSecretDeleteRequest)).thenReturn(true);
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteDropboxSecret(dropboxSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteDropboxSecret(dropboxSecretDeleteRequest);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    public void testDeleteDropboxSecret_CreateThrowsException() {
        SecretServiceHandler secretServiceHandler = Mockito.spy(new SecretServiceHandler());
        DropboxSecretDeleteRequest dropboxSecretDeleteRequest = Mockito.mock(DropboxSecretDeleteRequest.class);
        StreamObserver<Empty> streamObserver = Mockito.mock(StreamObserver.class);
        SecretBackend secretBackend = Mockito.mock(SecretBackend.class);

        try {
            Mockito.when(secretBackend.deleteDropboxSecret(dropboxSecretDeleteRequest)).thenThrow(new Exception());
            Mockito.doReturn(secretBackend).when(secretServiceHandler).getBackend();

            secretServiceHandler.deleteDropboxSecret(dropboxSecretDeleteRequest, streamObserver);

            Mockito.verify(secretBackend, Mockito.times(1)).deleteDropboxSecret(dropboxSecretDeleteRequest);
            Mockito.verify(streamObserver, Mockito.times(1)).onError(Mockito.any(StatusRuntimeException.class));
        } catch (Exception e) {
            fail(e);
        }
    }
}
