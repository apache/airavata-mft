package org.apache.airavata.mft.secret.server.backend.file;

import org.apache.airavata.mft.secret.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestFileBasedSecretBackend {

    static FileBasedSecretBackend fileBasedSecretBackend;

    @BeforeAll
    public static void before() {
        fileBasedSecretBackend = Mockito.spy(new FileBasedSecretBackend());
        Mockito.doReturn("secrets.json").when(fileBasedSecretBackend).getSecretFile();
    }

    @Test
    public void testGetSCPSecret_WithProperSecretId() {
        SCPSecretGetRequest SecretGetRequest = Mockito.mock(SCPSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn("WrongSecretId");

        try {
            assertTrue(fileBasedSecretBackend.getSCPSecret(SecretGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetSCPSecret_WithWrongSecretId() {
        String secretId = "local-ssh-cred";
        SCPSecretGetRequest secretGetRequest = Mockito.mock(SCPSecretGetRequest.class);
        Mockito.when(secretGetRequest.getSecretId()).thenReturn(secretId);

        try {
            Optional<SCPSecret> scpSecretOptional = fileBasedSecretBackend.getSCPSecret(secretGetRequest);
            assertTrue(scpSecretOptional.isPresent());

            SCPSecret scpSecret = scpSecretOptional.get();
            assertEquals(secretId, scpSecret.getSecretId());
            assertNotNull(scpSecret.getSecretId());
            assertNotNull(scpSecret.getPrivateKey());
            assertNotNull(scpSecret.getPublicKey());
            assertNotNull(scpSecret.getPassphrase());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetS3Secret_WithProperSecretId() {
        S3SecretGetRequest SecretGetRequest = Mockito.mock(S3SecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn("WrongSecretId");

        try {
            assertTrue(fileBasedSecretBackend.getS3Secret(SecretGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetS3Secret_WithWrongSecretId() {
        String SecretId = "s3-cred";
        S3SecretGetRequest SecretGetRequest = Mockito.mock(S3SecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn(SecretId);

        try {
            Optional<S3Secret> s3SecretOptional = fileBasedSecretBackend.getS3Secret(SecretGetRequest);
            assertTrue(s3SecretOptional.isPresent());

            S3Secret s3Secret = s3SecretOptional.get();
            assertEquals(SecretId, s3Secret.getSecretId());
            assertNotNull(s3Secret.getSecretId());
            assertNotNull(s3Secret.getAccessKey());
            assertNotNull(s3Secret.getSecretKey());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetBoxSecret_WithProperSecretId() {
        BoxSecretGetRequest SecretGetRequest = Mockito.mock(BoxSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn("WrongSecretId");

        try {
            assertTrue(fileBasedSecretBackend.getBoxSecret(SecretGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetBoxSecret_WithWrongSecretId() {
        String SecretId = "box-cred";
        BoxSecretGetRequest SecretGetRequest = Mockito.mock(BoxSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn(SecretId);

        try {
            Optional<BoxSecret> boxSecretOptional = fileBasedSecretBackend.getBoxSecret(SecretGetRequest);
            assertTrue(boxSecretOptional.isPresent());

            BoxSecret boxSecret = boxSecretOptional.get();
            assertEquals(SecretId, boxSecret.getSecretId());
            assertNotNull(boxSecret.getAccessToken());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetAzureSecret_WithProperSecretId() {
        AzureSecretGetRequest SecretGetRequest = Mockito.mock(AzureSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn("WrongSecretId");

        try {
            assertTrue(fileBasedSecretBackend.getAzureSecret(SecretGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetAzureSecret_WithWrongSecretId() {
        String SecretId = "azure-cred";
        AzureSecretGetRequest SecretGetRequest = Mockito.mock(AzureSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn(SecretId);

        try {
            Optional<AzureSecret> azureSecretOptional = fileBasedSecretBackend.getAzureSecret(SecretGetRequest);
            assertTrue(azureSecretOptional.isPresent());

            AzureSecret azureSecret = azureSecretOptional.get();
            assertEquals(SecretId, azureSecret.getSecretId());
            assertNotNull(azureSecret.getSecretId());
            assertNotNull(azureSecret.getConnectionString());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetGCSSecret_WithProperSecretId() {
        GCSSecretGetRequest SecretGetRequest = Mockito.mock(GCSSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn("WrongSecretId");

        try {
            assertTrue(fileBasedSecretBackend.getGCSSecret(SecretGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetGCSSecret_WithWrongSecretId() {
        String SecretId = "gcs-cred";
        GCSSecretGetRequest SecretGetRequest = Mockito.mock(GCSSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn(SecretId);

        try {
            Optional<GCSSecret> gcsSecretOptional = fileBasedSecretBackend.getGCSSecret(SecretGetRequest);
            assertTrue(gcsSecretOptional.isPresent());

            GCSSecret gcsSecret = gcsSecretOptional.get();
            assertEquals(SecretId, gcsSecret.getSecretId());
            assertNotNull(gcsSecret.getSecretId());
            assertNotNull(gcsSecret.getCredentialsJson());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetDropboxSecret_WithProperSecretId() {
        DropboxSecretGetRequest SecretGetRequest = Mockito.mock(DropboxSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn("WrongSecretId");

        try {
            assertTrue(fileBasedSecretBackend.getDropboxSecret(SecretGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetDropboxSecret_WithWrongSecretId() {
        String SecretId = "dropbox-cred";
        DropboxSecretGetRequest SecretGetRequest = Mockito.mock(DropboxSecretGetRequest.class);
        Mockito.when(SecretGetRequest.getSecretId()).thenReturn(SecretId);

        try {
            Optional<DropboxSecret> dropboxSecretOptional = fileBasedSecretBackend.getDropboxSecret(SecretGetRequest);
            assertTrue(dropboxSecretOptional.isPresent());

            DropboxSecret dropboxSecret = dropboxSecretOptional.get();
            assertEquals(SecretId, dropboxSecret.getSecretId());
            assertNotNull(dropboxSecret.getSecretId());
            assertNotNull(dropboxSecret.getAccessToken());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

}
