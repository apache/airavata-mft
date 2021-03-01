/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.resource.server.backend.airavata;

import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.stubs.azure.storage.*;
import org.apache.airavata.mft.resource.stubs.box.storage.*;
import org.apache.airavata.mft.resource.stubs.common.*;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.*;
import org.apache.airavata.mft.resource.stubs.ftp.storage.*;
import org.apache.airavata.mft.resource.stubs.gcs.storage.*;
import org.apache.airavata.mft.resource.stubs.local.storage.*;
import org.apache.airavata.mft.resource.stubs.s3.storage.*;
import org.apache.airavata.mft.resource.stubs.scp.storage.*;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.data.movement.DataMovementInterface;
import org.apache.airavata.model.data.movement.DataMovementProtocol;
import org.apache.airavata.model.data.movement.SCPDataMovement;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AiravataResourceBackend implements ResourceBackend {

    private static final Logger logger = LoggerFactory.getLogger(AiravataResourceBackend.class);

    @org.springframework.beans.factory.annotation.Value("${airavata.backend.registry.server.host}")
    private String registryServerHost;

    @org.springframework.beans.factory.annotation.Value("${airavata.backend.registry.server.port}")
    private int registryServerPort;

    @Override
    public void init() {
        logger.info("Initializing Airavata resource backend");
    }

    @Override
    public void destroy() {
        logger.info("Destroying Airavata resource backend");
    }

    @Override
    public Optional<GenericResource> getGenericResource(GenericResourceGetRequest request) throws Exception {
        String resourceId = request.getResourceId();
        String storageId = resourceId.substring(0, resourceId.lastIndexOf(":"));
        String path = resourceId.substring(resourceId.lastIndexOf(":") + 1);

        GenericResource scpResource = GenericResource.newBuilder()
                .setResourceId(resourceId)
                .setFile(FileResource.newBuilder().setResourcePath(path).build())
                .setScpStorage(getSCPStorage(SCPStorageGetRequest.newBuilder().setStorageId(storageId).build()).get())
                .build();
        return Optional.of(scpResource);
    }

    @Override
    public GenericResource createGenericResource(GenericResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGenericResource(GenericResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGenericResource(GenericResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) throws Exception {

        String resourceId = request.getStorageId();
        String[] parts = resourceId.split(":");
        String type = parts[0];
        String storageOrComputeId = parts[2];
        String user = parts[3];

        logger.info("Connecting to registry service {}:{}", registryServerHost, registryServerPort);

        RegistryService.Client registryClient = RegistryServiceClientFactory.createRegistryClient(registryServerHost, registryServerPort);
        SCPStorage.Builder builder = SCPStorage.newBuilder().setStorageId(resourceId);
        if ("STORAGE".equals(type)) {

            StorageResourceDescription storageResource = registryClient.getStorageResource(storageOrComputeId);

            Optional<DataMovementInterface> dmInterfaceOp = storageResource.getDataMovementInterfaces()
                    .stream().filter(iface -> iface.getDataMovementProtocol() == DataMovementProtocol.SCP).findFirst();

            DataMovementInterface scpInterface = dmInterfaceOp
                    .orElseThrow(() -> new Exception("Could not find a SCP interface for storage resource " + storageOrComputeId));

            SCPDataMovement scpDataMovement = registryClient.getSCPDataMovement(scpInterface.getDataMovementInterfaceId());

            String alternateHostName = scpDataMovement.getAlternativeSCPHostName();
            String selectedHostName = (alternateHostName == null || "".equals(alternateHostName))?
                    storageResource.getHostName() : alternateHostName;

            int selectedPort = scpDataMovement.getSshPort() == 0 ? 22 : scpDataMovement.getSshPort();

            builder.setHost(selectedHostName);
            builder.setPort(selectedPort);
            builder.setUser(user);

        } else if ("CLUSTER".equals(type)) {
            ComputeResourceDescription computeResource = registryClient.getComputeResource(storageOrComputeId);
            builder.setHost(computeResource.getHostName());
            builder.setPort(22);
            builder.setUser(user);
        }
        return Optional.of(builder.build());
    }

    @Override
    public SCPStorage createSCPStorage(SCPStorageCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateSCPStorage(SCPStorageUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteSCPStorage(SCPStorageDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public Optional<LocalStorage> getLocalStorage(LocalStorageGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public LocalStorage createLocalStorage(LocalStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateLocalStorage(LocalStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteLocalStorage(LocalStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<S3Storage> getS3Storage(S3StorageGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public S3Storage createS3Storage(S3StorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateS3Storage(S3StorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteS3Storage(S3StorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<BoxStorage> getBoxStorage(BoxStorageGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public BoxStorage createBoxStorage(BoxStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateBoxStorage(BoxStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteBoxStorage(BoxStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<AzureStorage> getAzureStorage(AzureStorageGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public AzureStorage createAzureStorage(AzureStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateAzureStorage(AzureStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteAzureStorage(AzureStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<GCSStorage> getGCSStorage(GCSStorageGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GCSStorage createGCSStorage(GCSStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSStorage(GCSStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSStorage(GCSStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<DropboxStorage> getDropboxStorage(DropboxStorageGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public DropboxStorage createDropboxStorage(DropboxStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateDropboxStorage(DropboxStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteDropboxStorage(DropboxStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<FTPStorage> getFTPStorage(FTPStorageGetRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public FTPStorage createFTPStorage(FTPStorageCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateFTPStorage(FTPStorageUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteFTPStorage(FTPStorageDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }
}
