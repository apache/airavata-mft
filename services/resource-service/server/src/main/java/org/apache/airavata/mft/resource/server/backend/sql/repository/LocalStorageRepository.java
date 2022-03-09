package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.LocalStorageEntity;
import org.springframework.data.repository.CrudRepository;

public interface LocalStorageRepository extends CrudRepository<LocalStorageEntity, String> {
}
