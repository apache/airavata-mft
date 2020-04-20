package org.apache.airavata.mft.transport.gcp;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;
import org.apache.airavata.mft.resource.client.ResourceServiceClient;
import org.apache.airavata.mft.resource.service.ResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.GCSResource;
import org.apache.airavata.mft.resource.service.GCSResourceGetRequest;
import org.apache.airavata.mft.resource.service.S3ResourceGetRequest;
import org.apache.airavata.mft.secret.client.SecretServiceClient;
import org.apache.airavata.mft.secret.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;


public class GCSReceiver implements Connector{

    private static final Logger logger = LoggerFactory.getLogger(GCSReceiver.class);

    private GCSResource gcsResource;
    private Storage storage;

    @Override
    public void init(String resourceId, String credentialToken, String resourceServiceHost, int resourceServicePort, String secretServiceHost, int secretServicePort) throws Exception {
        ResourceServiceGrpc.ResourceServiceBlockingStub resourceClient = ResourceServiceClient.buildClient(resourceServiceHost, resourceServicePort);
        this.gcsResource = resourceClient.getGCSResource(GCSResourceGetRequest.newBuilder().setResourceId(resourceId).build());

        SecretServiceGrpc.SecretServiceBlockingStub secretClient = SecretServiceClient.buildClient(secretServiceHost, secretServicePort);
        GCSSecret gcsSecret = secretClient.getGCSSecret(GCSSecretGetRequest.newBuilder().setSecretId(credentialToken).build());
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(gcsSecret.getJsonCredentialsFilePath()));
        if (credential.createScopedRequired()) {
            Collection<String> scopes = StorageScopes.all();
            credential = credential.createScoped(scopes);
        }
        storage=new Storage.Builder(transport, jsonFactory, credential).build();
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startStream(ConnectorContext context) throws Exception {
        logger.info("Starting GCS Receiver stream for transfer {}", context.getTransferId());

//        S3Object s3object = s3Client.getObject(s3Resource.getBucketName(), s3Resource.getResourcePath());
//        S3ObjectInputStream inputStream = s3object.getObjectContent();

        InputStream inputStream=storage.objects().get(gcsResource.getBucketName(),gcsResource.getResourcePath()).executeMediaAsInputStream();
        OutputStream os = context.getStreamBuffer().getOutputStream();
        int read;
        long bytes = 0;
        while ((read = inputStream.read()) != -1) {
            bytes++;
            os.write(read);
        }
        os.flush();
        os.close();

        logger.info("Completed GCS Receiver stream for transfer {}", context.getTransferId());
    }
}
