package org.apache.airavata.mft.secret.server.backend.sql.repository;

import org.apache.airavata.mft.secret.server.backend.sql.entity.FTPSecretEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FTPSecretRepository extends CrudRepository<FTPSecretEntity, String> {
    Optional<FTPSecretEntity> findBySecretId(String secretId);
}
