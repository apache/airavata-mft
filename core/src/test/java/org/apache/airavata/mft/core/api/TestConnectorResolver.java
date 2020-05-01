package org.apache.airavata.mft.core.api;

import org.apache.airavata.mft.core.ConnectorResolver;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestConnectorResolver {

    @Test
    public void testOutOfScopeConnector() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("outOfScope", "outOfScope");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testSCPIn() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("SCP", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.scp.SCPReceiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testSCPOut() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("SCP", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.scp.SCPSender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testSCP_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("SCP", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testLocalIn() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("LOCAL", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.local.LocalReceiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testLocalOut() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("LOCAL", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.local.LocalSender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testLocal_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("LOCAL", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testS3In() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("S3", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.s3.S3Receiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testS3Out() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("S3", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.s3.S3Sender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testS3_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("S3", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testBoxIn() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("BOX", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.box.BoxReceiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testBoxOut() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("BOX", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.box.BoxSender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testBox_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("BOX", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testAzureIn() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("AZURE", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.azure.AzureReceiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testAzureOut() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("AZURE", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.azure.AzureSender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testAzure_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("AZURE", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testGCSIn() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("GCS", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.gcp.GCSReceiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testGCSOut() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("GCS", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.gcp.GCSSender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testGCS_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("GCS", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Disabled("Ignoring because of dependency issues with dropbox modules")
    @Test
    public void testDropboxIn() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("DROPBOX", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.dropbox.DropboxReceiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Disabled("Ignoring because of dependency issues with dropbox modules")
    @Test
    public void testDropboxOut() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("DROPBOX", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.dropbox.DropboxSender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testDropbox_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("DROPBOX", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testFTPIn() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("FTP", "IN");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.ftp.FTPReceiver", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testFTPOut() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("FTP", "OUT");
            assertTrue(connector.isPresent());
            assertEquals("org.apache.airavata.mft.transport.ftp.FTPSender", connector.get().getClass().getCanonicalName());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }

    @Test
    public void testFTP_WrongDirection() {
        try {
            Optional<Connector> connector = ConnectorResolver.resolveConnector("FTP", "wrong input");
            assertTrue(connector.isEmpty());
        } catch(Exception ex) {
            fail("Exception from connector: ", ex);
        }
    }
}
