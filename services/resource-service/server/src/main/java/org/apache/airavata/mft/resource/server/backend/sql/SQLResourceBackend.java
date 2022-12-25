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
import org.apache.airavata.mft.resource.stubs.dropbox.storage.*;
import org.apache.airavata.mft.resource.stubs.ftp.storage.*;
import org.apache.airavata.mft.resource.stubs.gcs.storage.*;
import org.apache.airavata.mft.resource.stubs.local.storage.*;
import org.apache.airavata.mft.resource.stubs.odata.storage.*;
import org.apache.airavata.mft.resource.stubs.s3.storage.*;
import org.apache.airavata.mft.resource.stubs.scp.storage.*;
import org.apache.airavata.mft.resource.stubs.storage.common.*;
import org.apache.airavata.mft.resource.stubs.storage.common.Error;
import org.apache.airavata.mft.resource.stubs.swift.storage.*;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
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
    private GCSStorageRepository gcsStorageRepository;

    @Autowired
    private SwiftStorageRepository swiftStorageRepository;

    @Autowired
    private FTPStorageRepository ftpStorageRepository;

    @Autowired
    private LocalStorageRepository localStorageRepository;

    @Autowired
    private StorageSecretRepository resourceSecretRepository;

    @Autowired
    private ODataStorageRepository odataStorageRepository;

    @Autowired
    private ResolveStorageRepository resolveStorageRepository;

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
    public SecretForStorage getSecretForStorage(SecretForStorageGetRequest request) throws Exception {
        Optional<StorageSecretEntity> resourceSecEtyOp = resourceSecretRepository.findByStorageId(request.getStorageId());
        SecretForStorage.Builder resultBuilder = SecretForStorage.newBuilder();
        if (resourceSecEtyOp.isPresent()) {
            StorageSecretEntity storageSecretEntity = resourceSecEtyOp.get();
            resultBuilder.setSecretId(storageSecretEntity.getSecretId());
            resultBuilder.setStorageId(storageSecretEntity.getStorageId());
        } else {
            resultBuilder.setError(Error.NOT_FOUND);
        }
        return resultBuilder.build();
    }

    @Override
    public SecretForStorage registerSecretForStorage(SecretForStorage request) throws Exception {
        StorageSecretEntity ety = new StorageSecretEntity();
        ety.setSecretId(request.getSecretId());
        ety.setStorageId(request.getStorageId());
        ety.setType(request.getStorageType().name());
        resourceSecretRepository.save(ety);
        return request;
    }

    @Override
    public boolean deleteSecretForStorage(SecretForStorageDeleteRequest request) throws Exception {
        resourceSecretRepository.deleteByStorageId(request.getStorageId());
        return true;
    }

    @Override
    public StorageListResponse searchStorages(StorageSearchRequest request) throws Exception {
        StorageListResponse.Builder resp = StorageListResponse.newBuilder();
        switch (request.getSearchQueryCase()) {
            case STORAGEID:
                Optional<ResolveStorageEntity> storageOp = resolveStorageRepository.getByStorageId(request.getStorageId());
                if (storageOp.isPresent()) {
                    StorageListEntry.Builder entry = StorageListEntry.newBuilder();
                    entry.setStorageId(storageOp.get().getStorageId());
                    entry.setStorageName(storageOp.get().getStorageName());
                    entry.setStorageType(StorageType.valueOf(storageOp.get().getStorageType().name()));
                    resp.addStorageList(entry);
                }
                break;
            case STORAGENAME:
                List<ResolveStorageEntity> storages = resolveStorageRepository.getByStorageName(request.getStorageName());
                storages.forEach(st -> {
                    StorageListEntry.Builder entry = StorageListEntry.newBuilder();
                    entry.setStorageId(st.getStorageId());
                    entry.setStorageName(st.getStorageName());
                    entry.setStorageType(StorageType.valueOf(st.getStorageType().name()));
                    resp.addStorageList(entry);
                });
                break;
            case STORAGETYPE:
                storages = resolveStorageRepository.getByStorageType(ResolveStorageEntity.StorageType.valueOf(request.getStorageType().name()));
                storages.forEach(st -> {
                    StorageListEntry.Builder entry = StorageListEntry.newBuilder();
                    entry.setStorageId(st.getStorageId());
                    entry.setStorageName(st.getStorageName());
                    entry.setStorageType(StorageType.valueOf(st.getStorageType().name()));
                    resp.addStorageList(entry);
                });
                break;
        }
        return resp.build();
    }

    @Override
    public StorageListResponse listStorage(StorageListRequest request) throws Exception {
        Iterable<ResolveStorageEntity> all = resolveStorageRepository.findAll();
        StorageListResponse.Builder builder = StorageListResponse.newBuilder();
        all.forEach(r -> {
            StorageListEntry.Builder entry = StorageListEntry.newBuilder();
            entry.setStorageId(r.getStorageId());
            entry.setStorageType(StorageType.valueOf(r.getStorageType().name()));
            entry.setStorageName(r.getStorageName());
            builder.addStorageList(entry);
        });
        return builder.build();
    }

    @Override
    public SCPStorageListResponse listSCPStorage(SCPStorageListRequest request) throws Exception {
        SCPStorageListResponse.Builder respBuilder = SCPStorageListResponse.newBuilder();
        List<SCPStorageEntity> all = scpStorageRepository.findAll(PageRequest.of(request.getOffset(), request.getLimit()));
        all.forEach(ety -> respBuilder.addStorages(mapper.map(ety, SCPStorage.newBuilder().getClass())));
        return respBuilder.build();
    }

    @Override
    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) {
        Optional<SCPStorageEntity> storageEty = scpStorageRepository.findByStorageId(request.getStorageId());
        return storageEty.map(scpStorageEntity -> mapper.map(scpStorageEntity, SCPStorage.newBuilder().getClass()).build());
    }

    @Override
    public SCPStorage createSCPStorage(SCPStorageCreateRequest request) {
        SCPStorageEntity savedEntity = scpStorageRepository.save(mapper.map(request, SCPStorageEntity.class));

        ResolveStorageEntity storageTypeEty = new ResolveStorageEntity();
        storageTypeEty.setStorageId(savedEntity.getStorageId());
        storageTypeEty.setStorageType(ResolveStorageEntity.StorageType.SCP);
        storageTypeEty.setStorageName(savedEntity.getName());
        resolveStorageRepository.save(storageTypeEty);

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
    public LocalStorageListResponse listLocalStorage(LocalStorageListRequest request) throws Exception {
        LocalStorageListResponse.Builder respBuilder = LocalStorageListResponse.newBuilder();
        List<LocalStorageEntity> all = localStorageRepository.findAll(PageRequest.of(request.getOffset(), request.getLimit()));
        all.forEach(ety -> respBuilder.addStorages(mapper.map(ety, LocalStorage.newBuilder().getClass())));
        return respBuilder.build();
    }

    @Override
    public Optional<LocalStorage> getLocalStorage(LocalStorageGetRequest request) throws Exception {
        Optional<LocalStorageEntity> entity = localStorageRepository.findById(request.getStorageId());
        return entity.map(e -> mapper.map(e, LocalStorage.newBuilder().getClass()).build());
    }

    @Override
    public LocalStorage createLocalStorage(LocalStorageCreateRequest request) throws Exception {
        LocalStorageEntity savedEntity = localStorageRepository.save(mapper.map(request, LocalStorageEntity.class));

        ResolveStorageEntity storageTypeEty = new ResolveStorageEntity();
        storageTypeEty.setStorageId(savedEntity.getStorageId());
        storageTypeEty.setStorageType(ResolveStorageEntity.StorageType.LOCAL);
        storageTypeEty.setStorageName(savedEntity.getName());
        resolveStorageRepository.save(storageTypeEty);

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
    public S3StorageListResponse listS3Storage(S3StorageListRequest request) throws Exception {
        S3StorageListResponse.Builder respBuilder = S3StorageListResponse.newBuilder();
        List<S3StorageEntity> all = s3StorageRepository.findAll(PageRequest.of(request.getOffset(), request.getLimit()));
        all.forEach(ety -> respBuilder.addStorages(mapper.map(ety, S3Storage.newBuilder().getClass())));
        return respBuilder.build();
    }

    @Override
    public Optional<S3Storage> getS3Storage(S3StorageGetRequest request) throws Exception {
        Optional<S3StorageEntity> entity = s3StorageRepository.findById(request.getStorageId());
        return entity.map(e -> mapper.map(e, S3Storage.newBuilder().getClass()).build());
    }

    @Override
    public S3Storage createS3Storage(S3StorageCreateRequest request) throws Exception {
        S3StorageEntity savedEntity = s3StorageRepository.save(mapper.map(request, S3StorageEntity.class));

        ResolveStorageEntity storageTypeEty = new ResolveStorageEntity();
        storageTypeEty.setStorageId(savedEntity.getStorageId());
        storageTypeEty.setStorageType(ResolveStorageEntity.StorageType.S3);
        storageTypeEty.setStorageName(savedEntity.getName());
        resolveStorageRepository.save(storageTypeEty);

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
    public BoxStorageListResponse listBoxStorage(BoxStorageListRequest request) throws Exception {
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
    public AzureStorageListResponse listAzureStorage(AzureStorageListRequest request) throws Exception {
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
    public GCSStorageListResponse listGCSStorage(GCSStorageListRequest request) throws Exception {
        GCSStorageListResponse.Builder respBuilder = GCSStorageListResponse.newBuilder();
        List<GCSStorageEntity> all = gcsStorageRepository.findAll(PageRequest.of(request.getOffset(), request.getLimit()));
        all.forEach(ety -> respBuilder.addStorages(mapper.map(ety, GCSStorage.newBuilder().getClass())));
        return respBuilder.build();
    }

    @Override
    public Optional<GCSStorage> getGCSStorage(GCSStorageGetRequest request) throws Exception {
        Optional<GCSStorageEntity> entity = gcsStorageRepository.findById(request.getStorageId());
        return entity.map(e -> mapper.map(e, GCSStorage.newBuilder().getClass()).build());    }

    @Override
    public GCSStorage createGCSStorage(GCSStorageCreateRequest request) throws Exception {
        GCSStorageEntity savedEntity = gcsStorageRepository.save(mapper.map(request, GCSStorageEntity.class));

        ResolveStorageEntity storageTypeEty = new ResolveStorageEntity();
        storageTypeEty.setStorageId(savedEntity.getStorageId());
        storageTypeEty.setStorageType(ResolveStorageEntity.StorageType.GCS);
        storageTypeEty.setStorageName(savedEntity.getName());
        resolveStorageRepository.save(storageTypeEty);

        return mapper.map(savedEntity, GCSStorage.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateGCSStorage(GCSStorageUpdateRequest request) throws Exception {
        gcsStorageRepository.save(mapper.map(request, GCSStorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteGCSStorage(GCSStorageDeleteRequest request) throws Exception {
        gcsStorageRepository.deleteById(request.getStorageId());
        resourceRepository.deleteByStorageIdAndStorageType(request.getStorageId(), GenericResourceEntity.StorageType.GCS);
        return true;
    }

    @Override
    public DropboxStorageListResponse listDropboxStorage(DropboxStorageListRequest request) throws Exception {
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
    public FTPStorageListResponse listFTPStorage(FTPStorageListRequest request) throws Exception {
        FTPStorageListResponse.Builder respBuilder = FTPStorageListResponse.newBuilder();
        List<FTPStorageEntity> all = ftpStorageRepository.findAll(PageRequest.of(request.getOffset(), request.getLimit()));
        all.forEach(ety -> respBuilder.addStorages(mapper.map(ety, FTPStorage.newBuilder().getClass())));
        return respBuilder.build();
    }

    @Override
    public Optional<FTPStorage> getFTPStorage(FTPStorageGetRequest request) {
        Optional<FTPStorageEntity> entity = ftpStorageRepository.findByStorageId(request.getStorageId());
        return entity.map(e -> mapper.map(e, FTPStorage.newBuilder().getClass()).build());
    }

    @Override
    public FTPStorage createFTPStorage(FTPStorageCreateRequest request) {
        FTPStorageEntity savedEntity = ftpStorageRepository.save(mapper.map(request, FTPStorageEntity.class));

        ResolveStorageEntity storageTypeEty = new ResolveStorageEntity();
        storageTypeEty.setStorageId(savedEntity.getStorageId());
        storageTypeEty.setStorageType(ResolveStorageEntity.StorageType.FTP);
        storageTypeEty.setStorageName(savedEntity.getName());
        resolveStorageRepository.save(storageTypeEty);

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

    @Override
    public SwiftStorageListResponse listSwiftStorage(SwiftStorageListRequest request) throws Exception {
        SwiftStorageListResponse.Builder respBuilder = SwiftStorageListResponse.newBuilder();
        List<SwiftStorageEntity> all = swiftStorageRepository.findAll(PageRequest.of(request.getOffset(), request.getLimit()));
        all.forEach(ety -> respBuilder.addStorages(mapper.map(ety, SwiftStorage.newBuilder().getClass())));
        return respBuilder.build();
    }

    @Override
    public Optional<SwiftStorage> getSwiftStorage(SwiftStorageGetRequest request) throws Exception {
        Optional<SwiftStorageEntity> entity = swiftStorageRepository.findByStorageId(request.getStorageId());
        return entity.map(e -> mapper.map(e, SwiftStorage.newBuilder().getClass()).build());
    }

    @Override
    public SwiftStorage createSwiftStorage(SwiftStorageCreateRequest request) throws Exception {
        SwiftStorageEntity savedEntity = swiftStorageRepository.save(mapper.map(request, SwiftStorageEntity.class));

        ResolveStorageEntity storageTypeEty = new ResolveStorageEntity();
        storageTypeEty.setStorageId(savedEntity.getStorageId());
        storageTypeEty.setStorageType(ResolveStorageEntity.StorageType.SWIFT);
        storageTypeEty.setStorageName(savedEntity.getName());
        resolveStorageRepository.save(storageTypeEty);

        return mapper.map(savedEntity, SwiftStorage.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateSwiftStorage(SwiftStorageUpdateRequest request) throws Exception {
        swiftStorageRepository.save(mapper.map(request, SwiftStorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteSwiftStorage(SwiftStorageDeleteRequest request) throws Exception {
        swiftStorageRepository.deleteById(request.getStorageId());
        resourceRepository.deleteByStorageIdAndStorageType(request.getStorageId(), GenericResourceEntity.StorageType.SWIFT);
        return true;
    }

    @Override
    public ODataStorageListResponse listODataStorage(ODataStorageListRequest request) throws Exception {
        ODataStorageListResponse.Builder respBuilder = ODataStorageListResponse.newBuilder();
        List<ODataStorageEntity> all = odataStorageRepository.findAll(PageRequest.of(request.getOffset(), request.getLimit()));
        all.forEach(ety -> respBuilder.addStorages(mapper.map(ety, ODataStorage.newBuilder().getClass())));
        return respBuilder.build();
    }

    @Override
    public Optional<ODataStorage> getODataStorage(ODataStorageGetRequest request) throws Exception {
        Optional<ODataStorageEntity> entity = odataStorageRepository.findByStorageId(request.getStorageId());
        return entity.map(e -> mapper.map(e, ODataStorage.newBuilder().getClass()).build());
    }

    @Override
    public ODataStorage createODataStorage(ODataStorageCreateRequest request) throws Exception {
        ODataStorageEntity savedEntity = odataStorageRepository.save(mapper.map(request, ODataStorageEntity.class));

        ResolveStorageEntity storageTypeEty = new ResolveStorageEntity();
        storageTypeEty.setStorageId(savedEntity.getStorageId());
        storageTypeEty.setStorageType(ResolveStorageEntity.StorageType.ODATA);
        storageTypeEty.setStorageName(savedEntity.getName());
        resolveStorageRepository.save(storageTypeEty);

        return mapper.map(savedEntity, ODataStorage.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateODataStorage(ODataStorageUpdateRequest request) throws Exception {
        odataStorageRepository.save(mapper.map(request, ODataStorageEntity.class));
        return true;
    }

    @Override
    public boolean deleteODataStorage(ODataStorageDeleteRequest request) throws Exception {
        odataStorageRepository.deleteById(request.getStorageId());
        resourceRepository.deleteByStorageIdAndStorageType(request.getStorageId(), GenericResourceEntity.StorageType.SWIFT);
        return true;
    }

    @Override
    public StorageTypeResolveResponse resolveStorageType(StorageTypeResolveRequest request) throws Exception {
        Optional<ResolveStorageEntity> resolveStorageOp = resolveStorageRepository.getByStorageId(request.getStorageId());
        StorageTypeResolveResponse.Builder responseBuilder = StorageTypeResolveResponse.newBuilder();

        if (resolveStorageOp.isPresent()) {
            ResolveStorageEntity resolveStorageEntity = resolveStorageOp.get();
            responseBuilder.setStorageId(resolveStorageEntity.getStorageId());
            responseBuilder.setStorageType(StorageType.valueOf(resolveStorageEntity.getStorageType().name()));
            responseBuilder.setStorageName(resolveStorageEntity.getStorageName());
        } else {
            responseBuilder.setError(Error.NOT_FOUND);
        }
        return responseBuilder.build();
    }
}
