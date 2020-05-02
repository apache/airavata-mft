package org.apache.airavata.mft.resource.server.backend.file;

import org.apache.airavata.mft.resource.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestFileBasedResourceBackend {
    static FileBasedResourceBackend fileBasedResourceBackend;

    @BeforeAll
    public static void beforeClass() {
        fileBasedResourceBackend = new FileBasedResourceBackend();
        fileBasedResourceBackend.setResourceFile("resources.json");
    }

    @Test
    public void testGetFtpResource_WithProperResourceId() {
        String resourceId = "ftp-resource";
        FTPResourceGetRequest resourceGetRequest = Mockito.mock(FTPResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<FTPResource> ftpResourceOptional = fileBasedResourceBackend.getFTPResource(resourceGetRequest);
            assertTrue(ftpResourceOptional.isPresent());

            FTPResource ftpResource = ftpResourceOptional.get();
            assertEquals(resourceId, ftpResource.getResourceId());
            assertNotNull(ftpResource.getResourcePath());
            assertNotNull(ftpResource.getFtpStorage().getStorageId());
            assertNotNull(ftpResource.getFtpStorage().getHost());
            assertNotNull(ftpResource.getFtpStorage().getPort());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetFtpResource_WithWrongResourceId() {
        FTPResourceGetRequest resourceGetRequest = Mockito.mock(FTPResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getFTPResource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetSCPResource_WithProperResourceId() {
        SCPResourceGetRequest resourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getSCPResource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetSCPResource_WithWrongResourceId() {
        String resourceId = "remote-ssh-resource";
        SCPResourceGetRequest resourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<SCPResource> scpResourceOptional = fileBasedResourceBackend.getSCPResource(resourceGetRequest);
            assertTrue(scpResourceOptional.isPresent());

            SCPResource scpResource = scpResourceOptional.get();
            assertEquals(resourceId, scpResource.getResourceId());
            assertNotNull(scpResource.getResourcePath());
            assertNotNull(scpResource.getScpStorage().getStorageId());
            assertNotNull(scpResource.getScpStorage().getHost());
            assertNotNull(scpResource.getScpStorage().getPort());
            assertNotNull(scpResource.getScpStorage().getUser());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetLocalResource_WithProperResourceId() {
        LocalResourceGetRequest resourceGetRequest = Mockito.mock(LocalResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getLocalResource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetLocalResource_WithWrongResourceId() {
        String resourceId = "10mb-file";
        LocalResourceGetRequest resourceGetRequest = Mockito.mock(LocalResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<LocalResource> localResourceOptional = fileBasedResourceBackend.getLocalResource(resourceGetRequest);
            assertTrue(localResourceOptional.isPresent());

            LocalResource localResource = localResourceOptional.get();
            assertEquals(resourceId, localResource.getResourceId());
            assertNotNull(localResource.getResourcePath());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetS3Resource_WithProperResourceId() {
        S3ResourceGetRequest resourceGetRequest = Mockito.mock(S3ResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getS3Resource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetS3Resource_WithWrongResourceId() {
        String resourceId = "s3-file";
        S3ResourceGetRequest resourceGetRequest = Mockito.mock(S3ResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<S3Resource> s3ResourceOptional = fileBasedResourceBackend.getS3Resource(resourceGetRequest);
            assertTrue(s3ResourceOptional.isPresent());

            S3Resource s3Resource = s3ResourceOptional.get();
            assertEquals(resourceId, s3Resource.getResourceId());
            assertNotNull(s3Resource.getResourcePath());
            assertNotNull(s3Resource.getRegion());
            assertNotNull(s3Resource.getBucketName());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetBoxResource_WithProperResourceId() {
        BoxResourceGetRequest resourceGetRequest = Mockito.mock(BoxResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getBoxResource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetBoxResource_WithWrongResourceId() {
        String resourceId = "box-file-abcd";
        BoxResourceGetRequest resourceGetRequest = Mockito.mock(BoxResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<BoxResource> boxResourceOptional = fileBasedResourceBackend.getBoxResource(resourceGetRequest);
            assertTrue(boxResourceOptional.isPresent());

            BoxResource boxResource = boxResourceOptional.get();
            assertEquals(resourceId, boxResource.getResourceId());
            assertNotNull(boxResource.getBoxFileId());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetAzureResource_WithProperResourceId() {
        AzureResourceGetRequest resourceGetRequest = Mockito.mock(AzureResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getAzureResource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetAzureResource_WithWrongResourceId() {
        String resourceId = "azure-blob";
        AzureResourceGetRequest resourceGetRequest = Mockito.mock(AzureResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<AzureResource> azureResourceOptional = fileBasedResourceBackend.getAzureResource(resourceGetRequest);
            assertTrue(azureResourceOptional.isPresent());

            AzureResource azureResource = azureResourceOptional.get();
            assertEquals(resourceId, azureResource.getResourceId());
            assertNotNull(azureResource.getContainer());
            assertNotNull(azureResource.getBlobName());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetGCSResource_WithProperResourceId() {
        GCSResourceGetRequest resourceGetRequest = Mockito.mock(GCSResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getGCSResource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetGCSResource_WithWrongResourceId() {
        String resourceId = "gcs-bucket";
        GCSResourceGetRequest resourceGetRequest = Mockito.mock(GCSResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<GCSResource> gcsResourceOptional = fileBasedResourceBackend.getGCSResource(resourceGetRequest);
            assertTrue(gcsResourceOptional.isPresent());

            GCSResource gcsResource = gcsResourceOptional.get();
            assertEquals(resourceId, gcsResource.getResourceId());
            assertNotNull(gcsResource.getBucketName());
            assertNotNull(gcsResource.getResourcePath());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetDropboxResource_WithProperResourceId() {
        DropboxResourceGetRequest resourceGetRequest = Mockito.mock(DropboxResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn("WrongResourceId");

        try {
            assertTrue(fileBasedResourceBackend.getDropboxResource(resourceGetRequest).isEmpty());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

    @Test
    public void testGetDropboxResource_WithWrongResourceId() {
        String resourceId = "dropbox-file";
        DropboxResourceGetRequest resourceGetRequest = Mockito.mock(DropboxResourceGetRequest.class);
        Mockito.when(resourceGetRequest.getResourceId()).thenReturn(resourceId);

        try {
            Optional<DropboxResource> dropboxResourceOptional = fileBasedResourceBackend.getDropboxResource(resourceGetRequest);
            assertTrue(dropboxResourceOptional.isPresent());

            DropboxResource dropboxResource = dropboxResourceOptional.get();
            assertEquals(resourceId, dropboxResource.getResourceId());
            assertNotNull(dropboxResource.getResourcePath());
        } catch (Exception e) {
            fail("Exception from connector: ", e);
        }
    }

}
