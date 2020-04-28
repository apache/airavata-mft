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
import org.apache.airavata.mft.resource.server.backend.sql.entity.*;
import org.apache.airavata.mft.resource.server.backend.sql.repository.*;
import org.apache.airavata.mft.resource.service.*;
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
        scpStorageRepository.save(mapper.map(request, SCPStorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteSCPStorage(SCPStorageDeleteRequest request) {
        scpStorageRepository.deleteById(request.getStorageId());
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
        return getSCPResource(SCPResourceGetRequest.newBuilder().setResourceId(savedEntity.getResourceId()).build()).orElse(null);
    }

    @Override
    public boolean updateSCPResource(SCPResourceUpdateRequest request) {
        scpResourceRepository.save(mapper.map(request, SCPResourceEntity.class));
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
        localResourceRepository.save(mapper.map(request, LocalResourceEntity.class));
        return true;
    }

    @Override
    public boolean deleteLocalResource(LocalResourceDeleteRequest request) {
        localResourceRepository.deleteById(request.getResourceId());
        return true;
    }

    @Override
    public Optional<S3Resource> getS3Resource(S3ResourceGetRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public S3Resource createS3Resource(S3ResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateS3Resource(S3ResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteS3Resource(S3ResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public Optional<BoxResource> getBoxResource(BoxResourceGetRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public BoxResource createBoxResource(BoxResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateBoxResource(BoxResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteBoxResource(BoxResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<AzureResource> getAzureResource(AzureResourceGetRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public AzureResource createAzureResource(AzureResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateAzureResource(AzureResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteAzureResource(AzureResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<GCSResource> getGCSResource(GCSResourceGetRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GCSResource createGCSResource(GCSResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSResource(GCSResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSResource(GCSResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<DropboxResource> getDropboxResource(DropboxResourceGetRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public DropboxResource createDropboxResource(DropboxResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateDropboxResource(DropboxResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteDropboxResource(DropboxResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

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
}
