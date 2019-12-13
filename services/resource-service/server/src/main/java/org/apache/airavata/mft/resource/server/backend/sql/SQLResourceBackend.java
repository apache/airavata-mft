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
import org.apache.airavata.mft.resource.server.backend.sql.entity.LocalResourceEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.SCPResourceEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.SCPStorageEntity;
import org.apache.airavata.mft.resource.server.backend.sql.repository.LocalResourceRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.SCPResourceRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.SCPStorageRepository;
import org.apache.airavata.mft.resource.service.*;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class SQLResourceBackend implements ResourceBackend {

    @Autowired
    private SCPStorageRepository scpStorageRepository;

    @Autowired
    private SCPResourceRepository scpResourceRepository;

    @Autowired
    private LocalResourceRepository localResourceRepository;

    private DozerBeanMapper mapper = new DozerBeanMapper();

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
        scpStorageRepository.delete(request.getStorageId());
        return true;
    }

    @Override
    public Optional<SCPResource> getSCPResource(SCPResourceGetRequest request) {
        Optional<SCPResourceEntity> resourceEntity = scpResourceRepository.findByResourceId(request.getResourceId());
        return resourceEntity.map(scpResourceEntity -> mapper.map(scpResourceEntity, SCPResource.newBuilder().getClass()).build());
    }

    @Override
    public SCPResource createSCPResource(SCPResourceCreateRequest request) {
        SCPResourceEntity savedEntity = scpResourceRepository.save(mapper.map(request, SCPResourceEntity.class));
        return mapper.map(savedEntity, SCPResource.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateSCPResource(SCPResourceUpdateRequest request) {
        SCPResourceEntity updatedEntity = scpResourceRepository.save(mapper.map(request, SCPResourceEntity.class));
        return true;
    }

    @Override
    public boolean deleteSCPResource(SCPResourceDeleteRequest request) {
        scpResourceRepository.delete(request.getResourceId());
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
        localResourceRepository.delete(request.getResourceId());
        return true;
    }
}
