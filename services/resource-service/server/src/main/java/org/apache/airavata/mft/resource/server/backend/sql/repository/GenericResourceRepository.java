package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.GenericResourceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenericResourceRepository extends CrudRepository<GenericResourceEntity, String> {
    Optional<GenericResourceEntity> findByResourceId(String resourceId);
    List<GenericResourceEntity> findByStorageId(String storageId);
    void deleteByStorageIdAndStorageType(String storageId, GenericResourceEntity.StorageType storageType);
}
