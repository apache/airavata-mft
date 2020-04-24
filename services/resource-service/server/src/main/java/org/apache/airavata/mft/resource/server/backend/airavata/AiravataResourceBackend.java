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
import org.apache.airavata.mft.resource.service.*;
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

    @org.springframework.beans.factory.annotation.Value("${registry.server.host}")
    private String registryServerHost;

    @org.springframework.beans.factory.annotation.Value("${registry.server.port}")
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
    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) throws Exception {

        String resourceId = request.getStorageId();
        String[] parts = resourceId.split(":");
        String type = parts[0];
        String storageOrComputeId = parts[3];
        String user = parts[4];

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
    public Optional<SCPResource> getSCPResource(SCPResourceGetRequest request) throws Exception {
        String resourceId = request.getResourceId();
        String[] parts = resourceId.split(":");
        String path = parts[1];

        SCPResource scpResource = SCPResource.newBuilder()
                .setResourceId(resourceId)
                .setResourcePath(path)
                .setScpStorage(getSCPStorage(SCPStorageGetRequest.newBuilder().setStorageId(resourceId).build()).get())
                .build();
        return Optional.of(scpResource);
    }

    @Override
    public SCPResource createSCPResource(SCPResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateSCPResource(SCPResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteSCPResource(SCPResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public Optional<LocalResource> getLocalResource(LocalResourceGetRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public LocalResource createLocalResource(LocalResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateLocalResource(LocalResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteLocalResource(LocalResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public Optional<S3Resource> getS3Resource(S3ResourceGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public S3Resource createS3Resource(S3ResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateS3Resource(S3ResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteS3Resource(S3ResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public Optional<BoxResource> getBoxResource(BoxResourceGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public BoxResource createBoxResource(BoxResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateBoxResource(BoxResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteBoxResource(BoxResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<AzureResource> getAzureResource(AzureResourceGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public AzureResource createAzureResource(AzureResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateAzureResource(AzureResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteAzureResource(AzureResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<GCSResource> getGCSResource(GCSResourceGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GCSResource createGCSResource(GCSResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSResource(GCSResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSResource(GCSResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }
    @Override
    public Optional<DropboxResource> getDropboxResource(DropboxResourceGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public DropboxResource createDropboxResource(DropboxResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateDropboxResource(DropboxResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteDropboxResource(DropboxResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }
}
