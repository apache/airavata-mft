package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.S3StorageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface S3StorageRepository extends CrudRepository<S3StorageEntity, String> {
    List<S3StorageEntity> findAll(Pageable pageable);

}
