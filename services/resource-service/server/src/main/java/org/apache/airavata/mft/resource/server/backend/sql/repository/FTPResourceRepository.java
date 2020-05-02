package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.FTPResourceEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FTPResourceRepository extends CrudRepository<FTPResourceEntity, String> {
    Optional<FTPResourceEntity> findByResourceId(String resourceId);
}
