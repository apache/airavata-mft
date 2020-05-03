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

package org.apache.airavata.mft.resource.server.backend.sql;



import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.server.backend.sql.entity.FTPResourceEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.FTPStorageEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.LocalResourceEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.SCPResourceEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.SCPStorageEntity;
import org.apache.airavata.mft.resource.server.backend.sql.repository.FTPResourceRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.FTPStorageRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.LocalResourceRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.SCPResourceRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.SCPStorageRepository;
import org.apache.airavata.mft.resource.service.AzureResource;
import org.apache.airavata.mft.resource.service.AzureResourceCreateRequest;
import org.apache.airavata.mft.resource.service.AzureResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.AzureResourceGetRequest;
import org.apache.airavata.mft.resource.service.AzureResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.BoxResource;
import org.apache.airavata.mft.resource.service.BoxResourceCreateRequest;
import org.apache.airavata.mft.resource.service.BoxResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.BoxResourceGetRequest;
import org.apache.airavata.mft.resource.service.BoxResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.DropboxResource;
import org.apache.airavata.mft.resource.service.DropboxResourceCreateRequest;
import org.apache.airavata.mft.resource.service.DropboxResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.DropboxResourceGetRequest;
import org.apache.airavata.mft.resource.service.DropboxResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.FTPResource;
import org.apache.airavata.mft.resource.service.FTPResourceCreateRequest;
import org.apache.airavata.mft.resource.service.FTPResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.FTPResourceGetRequest;
import org.apache.airavata.mft.resource.service.FTPResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.FTPStorage;
import org.apache.airavata.mft.resource.service.FTPStorageCreateRequest;
import org.apache.airavata.mft.resource.service.FTPStorageDeleteRequest;
import org.apache.airavata.mft.resource.service.FTPStorageGetRequest;
import org.apache.airavata.mft.resource.service.FTPStorageUpdateRequest;
import org.apache.airavata.mft.resource.service.GCSResource;
import org.apache.airavata.mft.resource.service.GCSResourceCreateRequest;
import org.apache.airavata.mft.resource.service.GCSResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.GCSResourceGetRequest;
import org.apache.airavata.mft.resource.service.GCSResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.LocalResource;
import org.apache.airavata.mft.resource.service.LocalResourceCreateRequest;
import org.apache.airavata.mft.resource.service.LocalResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.LocalResourceGetRequest;
import org.apache.airavata.mft.resource.service.LocalResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.S3Resource;
import org.apache.airavata.mft.resource.service.S3ResourceCreateRequest;
import org.apache.airavata.mft.resource.service.S3ResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.S3ResourceGetRequest;
import org.apache.airavata.mft.resource.service.S3ResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.SCPResource;
import org.apache.airavata.mft.resource.service.SCPResourceCreateRequest;
import org.apache.airavata.mft.resource.service.SCPResourceDeleteRequest;
import org.apache.airavata.mft.resource.service.SCPResourceGetRequest;
import org.apache.airavata.mft.resource.service.SCPResourceUpdateRequest;
import org.apache.airavata.mft.resource.service.SCPStorage;
import org.apache.airavata.mft.resource.service.SCPStorageCreateRequest;
import org.apache.airavata.mft.resource.service.SCPStorageDeleteRequest;
import org.apache.airavata.mft.resource.service.SCPStorageGetRequest;
import org.apache.airavata.mft.resource.service.SCPStorageUpdateRequest;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class SQLResourceBackend implements ResourceBackend {

    private static final Logger logger = LoggerFactory.getLogger(SQLResourceBackend.class);

    @Autowired
    private SCPStorageRepository scpStorageRepository;

    @Autowired
    private SCPResourceRepository scpResourceRepository;

    @Autowired
    private LocalResourceRepository localResourceRepository;

    @Autowired
    private FTPResourceRepository ftpResourceRepository;

    @Autowired
    private FTPStorageRepository ftpStorageRepository;

    private DozerBeanMapper mapper = new DozerBeanMapper();

    @Override
    public void init() {
        logger.info("Initializing database resource backend");
    }

    @Override
    public void destroy() {
        logger.info("Destroying database resource backend");
    }

    @Override
    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) {
        Optional<SCPStorageEntity> storageEty = scpStorageRepository.findByStorageId(request.getStorageId());
        return storageEty.map(scpStorageEntity -> mapper.map(scpStorageEntity, SCPStorage.newBuilder().getClass()).build());
    }

    @Override
    public SCPStorage createSCPStorage(SCPStorageCreateRequest request) {
        SCPStorageEntity savedEntity = scpStorageRepository.save(mapper.map(request, SCPStorageEntity.class));
        return mapper.map(savedEntity, SCPStorage.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateSCPStorage(SCPStorageUpdateRequest request) {
        SCPStorageEntity updatedEntity = scpStorageRepository.save(mapper.map(request, SCPStorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteSCPStorage(SCPStorageDeleteRequest request) {
        //scpStorageRepository.delete(request.getStorageId());
        return true;
    }

    @Override
    public Optional<SCPResource> getSCPResource(SCPResourceGetRequest request) {
        Optional<SCPResourceEntity> resourceEntity = scpResourceRepository.findByResourceId(request.getResourceId());

        return resourceEntity.map(scpResourceEntity -> mapper.map(scpResourceEntity, SCPResource.newBuilder().getClass())
                .setScpStorage(mapper.map(scpResourceEntity.getScpStorage(), SCPStorage.newBuilder().getClass())).build());
        // Here we have to do nested mapping as the dozer -> protobuf conversion is not happening for inner objects
    }

    @Override
    public SCPResource createSCPResource(SCPResourceCreateRequest request) {
        SCPResourceEntity savedEntity = scpResourceRepository.save(mapper.map(request, SCPResourceEntity.class));
        return getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(savedEntity.getResourceId()).build()).get();
    }

    @Override
    public boolean updateSCPResource(SCPResourceUpdateRequest request) {
        SCPResourceEntity updatedEntity = scpResourceRepository.save(mapper.map(request, SCPResourceEntity.class));
        return true;
    }

    @Override
    public boolean deleteSCPResource(SCPResourceDeleteRequest request) {
        scpResourceRepository.deleteById(request.getResourceId());
        return true;
    }

    @Override
    public Optional<LocalResource> getLocalResource(LocalResourceGetRequest request) {
        Optional<LocalResourceEntity> resourceEntity = localResourceRepository.findByResourceId(request.getResourceId());
        return resourceEntity.map(scpResourceEntity -> mapper.map(scpResourceEntity, LocalResource.newBuilder().getClass()).build());
    }

    @Override
    public LocalResource createLocalResource(LocalResourceCreateRequest request) {
        LocalResourceEntity savedEntity = localResourceRepository.save(mapper.map(request, LocalResourceEntity.class));
        return mapper.map(savedEntity, LocalResource.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateLocalResource(LocalResourceUpdateRequest request) {
        LocalResourceEntity updatedEntity = localResourceRepository.save(mapper.map(request, LocalResourceEntity.class));
        return true;
    }

    @Override
    public boolean deleteLocalResource(LocalResourceDeleteRequest request) {
        localResourceRepository.deleteById(request.getResourceId());
        return true;
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

    @Override
    public Optional<FTPResource> getFTPResource(FTPResourceGetRequest request) {
        Optional<FTPResourceEntity> resourceEntity = ftpResourceRepository.findByResourceId(request.getResourceId());

        return resourceEntity.map(ftpResourceEntity -> mapper.map(ftpResourceEntity, FTPResource.newBuilder().getClass())
                .setFtpStorage(mapper.map(ftpResourceEntity.getFtpStorage(), FTPStorage.newBuilder().getClass())).build());
    }

    @Override
    public FTPResource createFTPResource(FTPResourceCreateRequest request) {
        FTPResourceEntity savedEntity = ftpResourceRepository.save(mapper.map(request, FTPResourceEntity.class));
        return getFTPResource(FTPResourceGetRequest.newBuilder().setResourceId(savedEntity.getResourceId()).build()).orElse(null);
    }

    @Override
    public boolean updateFTPResource(FTPResourceUpdateRequest request) {
        ftpResourceRepository.save(mapper.map(request, FTPResourceEntity.class));
        return true;
    }

    @Override
    public boolean deleteFTPResource(FTPResourceDeleteRequest request) {
        ftpResourceRepository.deleteById(request.getResourceId());
        return true;
    }

    @Override
    public Optional<FTPStorage> getFTPStorage(FTPStorageGetRequest request) {
        Optional<FTPStorageEntity> storageEty = ftpStorageRepository.findByStorageId(request.getStorageId());
        return storageEty.map(ftpStorageEntity -> mapper.map(ftpStorageEntity, FTPStorage.newBuilder().getClass()).build());
    }

    @Override
    public FTPStorage createFTPStorage(FTPStorageCreateRequest request) {
        FTPStorageEntity savedEntity = ftpStorageRepository.save(mapper.map(request, FTPStorageEntity.class));
        return mapper.map(savedEntity, FTPStorage.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateFTPStorage(FTPStorageUpdateRequest request) {
        ftpStorageRepository.save(mapper.map(request, FTPStorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteFTPStorage(FTPStorageDeleteRequest request) {
        ftpResourceRepository.deleteById(request.getStorageId());
        return true;
    }

    @Override
    public Optional<GDriveResource> getGDriveResource(GDriveResourceGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GDriveResource createGDriveResource(GDriveResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGDriveResource(GDriveResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGDriveResource(GDriveResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }
}
