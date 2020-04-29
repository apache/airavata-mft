package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.FTPStorageEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FTPStorageRepository extends CrudRepository<FTPStorageEntity, String> {
    Optional<FTPStorageEntity> findByStorageId(String storageId);
}
