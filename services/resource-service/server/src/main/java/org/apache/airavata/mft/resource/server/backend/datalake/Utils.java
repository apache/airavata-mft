package org.apache.airavata.mft.resource.server.backend.datalake;

import org.apache.airavata.datalake.drms.resource.GenericResource;
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.s3.S3StoragePreference;
import org.apache.airavata.datalake.drms.storage.preference.ssh.SSHStoragePreference;
import org.apache.airavata.drms.core.Neo4JConnector;
import org.apache.airavata.mft.resource.stubs.common.DirectoryResource;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.scp.storage.SCPStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final Neo4JConnector neo4JConnector = new Neo4JConnector();

    private static String datalakeURI;
    private static String datalakeUsername;
    private static String datalakePassword;

    public static void initializeDatalakeConnector(String uri, String username, String password) {
        neo4JConnector.init(uri, username, password);
        datalakeURI = uri;
        datalakeUsername = username;
        datalakePassword = password;
    }

    public synchronized static Neo4JConnector getNeo4JConnector() {
        if (neo4JConnector.isOpen()) {
            return neo4JConnector;
        } else {
            neo4JConnector.init(datalakeURI, datalakeUsername, datalakePassword);
            return neo4JConnector;
        }
    }

    public static org.apache.airavata.mft.resource.stubs.common.GenericResource transform(GenericResource resource,
                                                                                          AnyStoragePreference anyStoragePreference,
                                                                                          org.apache.airavata.mft.resource.stubs.common.GenericResource res) {
        org.apache.airavata.mft.resource.stubs.common.GenericResource.Builder grBuilder = res.toBuilder()
                .setResourceId(resource.getResourceId());
        if (anyStoragePreference.getSshStoragePreference() != null &&
                anyStoragePreference.getSshStoragePreference().hasStorage()) {
            SSHStoragePreference sshStoragePreference = anyStoragePreference.getSshStoragePreference();
            grBuilder = grBuilder.setScpStorage(SCPStorage.newBuilder()
                    .setHost(sshStoragePreference.getStorage().getHostName())
                    .setPort(sshStoragePreference.getStorage().getPort())
                    .setStorageId(sshStoragePreference.getStorage().getStorageId())
                    .build());
        } else if (anyStoragePreference.getS3StoragePreference() != null &&
                anyStoragePreference.getS3StoragePreference().hasStorage()) {
            S3StoragePreference sshStoragePreference = anyStoragePreference.getS3StoragePreference();
            grBuilder = grBuilder.setS3Storage(S3Storage
                    .newBuilder()
                    .setBucketName(sshStoragePreference.getStorage().getBucketName())
                    .setRegion(sshStoragePreference.getStorage().getRegion())
                    .setStorageId(sshStoragePreference.getStorage().getStorageId()).build());

        }
        if (resource.getType().equals("FILE")) {
            grBuilder = grBuilder.setFile(FileResource.newBuilder().setResourcePath(resource.getResourcePath()).build());
        } else {
            grBuilder = grBuilder
                    .setDirectory(DirectoryResource.newBuilder().setResourcePath(resource.getResourcePath()).build());
        }
        return grBuilder.build();

    }


}
