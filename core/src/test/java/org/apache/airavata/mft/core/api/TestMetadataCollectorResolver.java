package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.core.MetadataCollectorResolver;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestMetadataCollectorResolver {

    @Test
    public void testOutOfScopeConnector() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("outOfScope");
            assertTrue(connector.isEmpty());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testSCP() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("SCP");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.scp.SCPMetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testLocal() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("LOCAL");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.local.LocalMetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }
    @Test
    public void testS3() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("S3");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.s3.S3MetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testBox() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("BOX");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.box.BoxMetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testAzure() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("AZURE");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.azure.AzureMetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testGCS() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("GCS");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.gcp.GCSMetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Disabled("Ignoring because of dependency issues with dropbox modules")
    @Test
    public void testDropbox() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("DROPBOX");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.dropbox.DropboxMetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testFTP() {
        try {
            Optional<MetadataCollector> connector = MetadataCollectorResolver.resolveMetadataCollector("FTP");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.ftp.FTPMetadataCollector", connector.get().getClass().getCanonicalName());
        } catch (Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }
}
