package org.apache.airavata.mft.resource.server.backend.datalake;

import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.drms.core.deserializer.AnyStoragePreferenceDeserializer;
import org.apache.airavata.drms.core.deserializer.GenericResourceDeserializer;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.server.backend.file.FileBasedResourceBackend;
import org.apache.airavata.mft.resource.stubs.azure.storage.*;
import org.apache.airavata.mft.resource.stubs.box.storage.*;
import org.apache.airavata.mft.resource.stubs.common.*;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.*;
import org.apache.airavata.mft.resource.stubs.ftp.storage.*;
import org.apache.airavata.mft.resource.stubs.gcs.storage.*;
import org.apache.airavata.mft.resource.stubs.local.storage.*;
import org.apache.airavata.mft.resource.stubs.s3.storage.*;
import org.apache.airavata.mft.resource.stubs.scp.storage.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Connects to airavata datalake graph db to manage resources
 */
public class DatalakeResourceBackend implements ResourceBackend {
    private static final Logger logger = LoggerFactory.getLogger(FileBasedResourceBackend.class);

    @Value("${datalake.db.uri}")
    private String dbURI;

    @Value("${datalake.db.user}")
    private String dbUser;

    @Value("${datalake.db.password}")
    private String dbPassword;


    @Override
    public void init() {
        Utils.initializeDatalakeConnector(dbURI, dbUser, dbPassword);
    }

    @Override
    public void destroy() {
        logger.info("Destroying datalake resource backend");
    }

    @Override
    public Optional<GenericResource> getGenericResource(GenericResourceGetRequest request) throws Exception {

        try {
            String resourceId = request.getResourceId();
            Neo4JConnector connector = Utils.getNeo4JConnector();
            String query = "MATCH (place) where place.entityId='" + request.getResourceId() + "' return place";
            AtomicReference<GenericResource> resource = new AtomicReference<>(GenericResource.newBuilder().build());
            List<Record> recordList = connector.searchNodes(query);
            if (!recordList.isEmpty()) {
                Record record = recordList.get(0);
                List<org.neo4j.driver.Value> values = record.values();
                if (!values.isEmpty()) {
                    Node node = values.get(0).asNode();
                    Iterable<String> iterable = node.labels();
                    iterable.forEach(label -> {
                        String storagePreQuery = "Match (s:Storage)-[r1:HAS_PREFERENCE]->" +
                                "(sp:StoragePreference)-[r2:HAS_RESOURCE]->(res:" + label + "{entityId: '" + resourceId + "'}) " +
                                "return distinct sp, s";
                        List<Record> storageList = connector.searchNodes(storagePreQuery);
                        try {
                            List<AnyStoragePreference> preferences = AnyStoragePreferenceDeserializer
                                    .deserializeList(storageList);
                            List<org.apache.airavata.datalake.drms.resource.GenericResource> resourceList =
                                    GenericResourceDeserializer.deserializeList(recordList);
                            if (!resourceList.isEmpty()) {
                                org.apache.airavata.datalake.drms.resource.GenericResource genericResource =
                                        resourceList.get(0);
                                preferences.forEach(preference -> {
                                    resource.set(Utils.transform(genericResource, preference, resource.get()));
                                });

                            }
                        } catch (Exception exception) {
                            String msg = "Error occurred while deserializing records from datalake DB " + exception.getMessage();
                            logger.error(msg, exception);
                            throw new RuntimeException(msg, exception);
                        }
                    });
                }
            }
            return Optional.ofNullable(resource.get());
        } catch (Exception ex) {
            String msg = "Error occurred while fetching generic resource fom datalake DB " + ex.getMessage();
            logger.error(msg, ex);
            throw new RuntimeException(msg, ex);
        }
    }

    @Override
    public GenericResource createGenericResource(GenericResourceCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateGenericResource(GenericResourceUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteGenericResource(GenericResourceDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public SCPStorage createSCPStorage(SCPStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateSCPStorage(SCPStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteSCPStorage(SCPStorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<LocalStorage> getLocalStorage(LocalStorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public LocalStorage createLocalStorage(LocalStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateLocalStorage(LocalStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteLocalStorage(LocalStorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<S3Storage> getS3Storage(S3StorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public S3Storage createS3Storage(S3StorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateS3Storage(S3StorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteS3Storage(S3StorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<BoxStorage> getBoxStorage(BoxStorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public BoxStorage createBoxStorage(BoxStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateBoxStorage(BoxStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteBoxStorage(BoxStorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<AzureStorage> getAzureStorage(AzureStorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public AzureStorage createAzureStorage(AzureStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateAzureStorage(AzureStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteAzureStorage(AzureStorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<GCSStorage> getGCSStorage(GCSStorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public GCSStorage createGCSStorage(GCSStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateGCSStorage(GCSStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteGCSStorage(GCSStorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<DropboxStorage> getDropboxStorage(DropboxStorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public DropboxStorage createDropboxStorage(DropboxStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateDropboxStorage(DropboxStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteDropboxStorage(DropboxStorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<FTPStorage> getFTPStorage(FTPStorageGetRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public FTPStorage createFTPStorage(FTPStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateFTPStorage(FTPStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteFTPStorage(FTPStorageDeleteRequest request) throws Exception {
        return false;
    }
}
