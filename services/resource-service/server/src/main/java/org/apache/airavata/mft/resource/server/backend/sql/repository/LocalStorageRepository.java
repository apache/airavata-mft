package org.apache.airavata.mft.resource.server.backend.sql.repository;

import org.apache.airavata.mft.resource.server.backend.sql.entity.LocalStorageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalStorageRepository extends CrudRepository<LocalStorageEntity, String> {
    List<LocalStorageEntity> findAll(Pageable pageable);
}
