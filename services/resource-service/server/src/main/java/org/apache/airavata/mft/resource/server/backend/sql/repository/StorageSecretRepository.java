package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.StorageSecretEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StorageSecretRepository extends CrudRepository<StorageSecretEntity, String> {

    Optional<StorageSecretEntity> findByStorageId(String storageId);
    void deleteByStorageId(String resourceId);
}
