package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.GenericResourceEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GenericResourceRepository extends CrudRepository<GenericResourceEntity, String> {
    public Optional<GenericResourceEntity> findByResourceId(String resourceId);
    public List<GenericResourceEntity> findByStorageId(String storageId);
    public void deleteByStorageIdAndStorageType(String storageId, GenericResourceEntity.StorageType storageType);
}
