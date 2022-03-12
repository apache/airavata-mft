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
import org.apache.airavata.mft.resource.stubs.azure.storage.*;
import org.apache.airavata.mft.resource.stubs.box.storage.*;
import org.apache.airavata.mft.resource.stubs.common.*;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.*;
import org.apache.airavata.mft.resource.stubs.ftp.storage.*;
import org.apache.airavata.mft.resource.stubs.gcs.storage.*;
import org.apache.airavata.mft.resource.stubs.local.storage.*;
import org.apache.airavata.mft.resource.stubs.s3.storage.*;
import org.apache.airavata.mft.resource.stubs.scp.storage.*;
import org.apache.airavata.mft.storage.stubs.storagesecret.*;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class SQLResourceBackend implements ResourceBackend {

    private static final Logger logger = LoggerFactory.getLogger(SQLResourceBackend.class);

    @Autowired
    private GenericResourceRepository resourceRepository;

    @Autowired
    private SCPStorageRepository scpStorageRepository;

    @Autowired
    private S3StorageRepository s3StorageRepository;

    @Autowired
    private FTPStorageRepository ftpStorageRepository;

    @Autowired
    private LocalStorageRepository localStorageRepository;

    @Autowired
    private StorageSecretRepository resourceSecretRepository;

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
    public Optional<GenericResource> getGenericResource(GenericResourceGetRequest request) throws Exception {
        Optional<GenericResourceEntity> resourceEtyOp = resourceRepository.findByResourceId(request.getResourceId());
        if (resourceEtyOp.isPresent()) {

            GenericResourceEntity resourceEty = resourceEtyOp.get();
                    GenericResource.Builder builder = GenericResource.newBuilder();
            builder.setResourceId(resourceEty.getResourceId());
            switch (resourceEty.getResourceType()){
                case DIRECTORY:
                    builder.setDirectory(DirectoryResource.newBuilder().setResourcePath(resourceEty.getResourcePath()).build());
                    break;
                case FILE:
                    builder.setFile(FileResource.newBuilder().setResourcePath(resourceEty.getResourcePath()).build());
                    break;
            }

            switch (resourceEty.getStorageType()) {
                case S3:
                    Optional<S3Storage> s3Storage = getS3Storage(S3StorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setS3Storage(s3Storage.orElseThrow(() -> new Exception("Could not find a S3 storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
                case SCP:
                    Optional<SCPStorage> scpStorage = getSCPStorage(SCPStorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setScpStorage(scpStorage.orElseThrow(() -> new Exception("Could not find a SCP storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
                case LOCAL:
                    Optional<LocalStorage> localStorage = getLocalStorage(LocalStorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setLocalStorage(localStorage.orElseThrow(() -> new Exception("Could not find a Local storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
                case FTP:
                    Optional<FTPStorage> ftpStorage = getFTPStorage(FTPStorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setFtpStorage(ftpStorage.orElseThrow(() -> new Exception("Could not find a FTP storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
                case BOX:
                    Optional<BoxStorage> boxStorage = getBoxStorage(BoxStorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setBoxStorage(boxStorage.orElseThrow(() -> new Exception("Could not find a Box storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
                case DROPBOX:
                    Optional<DropboxStorage> dropBoxStorage = getDropboxStorage(DropboxStorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setDropboxStorage(dropBoxStorage.orElseThrow(() -> new Exception("Could not find a Dropbox storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
                case GCS:
                    Optional<GCSStorage> gcsStorage = getGCSStorage(GCSStorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setGcsStorage(gcsStorage.orElseThrow(() -> new Exception("Could not find a GCS storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
                case AZURE:
                    Optional<AzureStorage> azureStorage = getAzureStorage(AzureStorageGetRequest.newBuilder()
                            .setStorageId(resourceEty.getStorageId()).build());
                    builder.setAzureStorage(azureStorage.orElseThrow(() -> new Exception("Could not find a Azure storage with id "
                            + resourceEty.getStorageId() + " for resource " + resourceEty.getResourceId())));
                    break;
            }

            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public GenericResource createGenericResource(GenericResourceCreateRequest request) throws Exception {

        GenericResourceEntity entity = new GenericResourceEntity();
        entity.setStorageId(request.getStorageId());

        switch (request.getResourceCase()) {
            case FILE:
                entity.setResourcePath(request.getFile().getResourcePath());
                break;
            case DIRECTORY:
                entity.setResourcePath(request.getDirectory().getResourcePath());
                break;
        }

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
    public Optional<StorageSecret> getStorageSecret(StorageSecretGetRequest request) throws Exception {
        Optional<StorageSecretEntity> resourceSecEty = resourceSecretRepository.findById(request.getId());
        return resourceSecEty.map(ety -> mapper.map(ety, StorageSecret.newBuilder().getClass()).build());
    }

    @Override
    public StorageSecret createStorageSecret(StorageSecretCreateRequest request) throws Exception {
        StorageSecretEntity savedEntity = resourceSecretRepository.save(mapper.map(request, StorageSecretEntity.class));
        return mapper.map(savedEntity, StorageSecret.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateStorageSecret(StorageSecretUpdateRequest request) throws Exception {
        resourceSecretRepository.save(mapper.map(request, StorageSecretEntity.class));
        return true;
    }

    @Override
    public boolean deleteStorageSecret(StorageSecretDeleteRequest request) throws Exception {
        resourceSecretRepository.deleteById(request.getId());
        return false;
    }

    @Override
    public Optional<StorageSecret> searchStorageSecret(StorageSecretSearchRequest request) throws Exception {
        //resourceSecretRepository.findByStorageId();
        return Optional.empty();
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
        scpStorageRepository.deleteById(request.getStorageId());
        resourceRepository.deleteByStorageIdAndStorageType(request.getStorageId(), GenericResourceEntity.StorageType.SCP);
        return true;
    }

    @Override
    public Optional<LocalStorage> getLocalStorage(LocalStorageGetRequest request) throws Exception {
        Optional<LocalStorageEntity> entity = localStorageRepository.findById(request.getStorageId());
        return entity.map(e -> mapper.map(e, LocalStorage.newBuilder().getClass()).build());
    }

    @Override
    public LocalStorage createLocalStorage(LocalStorageCreateRequest request) throws Exception {
        LocalStorageEntity savedEntity = localStorageRepository.save(mapper.map(request, LocalStorageEntity.class));
        return mapper.map(savedEntity, LocalStorage.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateLocalStorage(LocalStorageUpdateRequest request) throws Exception {
        localStorageRepository.save(mapper.map(request, LocalStorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteLocalStorage(LocalStorageDeleteRequest request) throws Exception {
        localStorageRepository.deleteById(request.getStorageId());
        resourceRepository.deleteByStorageIdAndStorageType(request.getStorageId(), GenericResourceEntity.StorageType.LOCAL);
        return true;
    }

    @Override
    public Optional<S3Storage> getS3Storage(S3StorageGetRequest request) throws Exception {
        Optional<S3StorageEntity> entity = s3StorageRepository.findById(request.getStorageId());
        return entity.map(e -> mapper.map(e, S3Storage.newBuilder().getClass()).build());
    }

    @Override
    public S3Storage createS3Storage(S3StorageCreateRequest request) throws Exception {
        S3StorageEntity savedEntity = s3StorageRepository.save(mapper.map(request, S3StorageEntity.class));
        return mapper.map(savedEntity, S3Storage.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateS3Storage(S3StorageUpdateRequest request) throws Exception {
        s3StorageRepository.save(mapper.map(request, S3StorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteS3Storage(S3StorageDeleteRequest request) throws Exception {
        s3StorageRepository.deleteById(request.getStorageId());
        resourceRepository.deleteByStorageIdAndStorageType(request.getStorageId(), GenericResourceEntity.StorageType.S3);
        return true;
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
        Optional<FTPStorageEntity> entity = ftpStorageRepository.findByStorageId(request.getStorageId());
        return entity.map(e -> mapper.map(e, FTPStorage.newBuilder().getClass()).build());
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
        ftpStorageRepository.deleteById(request.getStorageId());
        resourceRepository.deleteByStorageIdAndStorageType(request.getStorageId(), GenericResourceEntity.StorageType.FTP);
        return true;
    }

}
