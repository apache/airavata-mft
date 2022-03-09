package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.S3StorageEntity;
import org.springframework.data.repository.CrudRepository;

public interface S3StorageRepository extends CrudRepository<S3StorageEntity, String> {

}
