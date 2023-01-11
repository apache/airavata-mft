package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.StorageSecretEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StorageSecretRepository extends CrudRepository<StorageSecretEntity, String> {

    public Optional<StorageSecretEntity> findByStorageId(String storageId);
    public void deleteByStorageId(String resourceId);
}
