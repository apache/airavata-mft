package org.apache.airavata.mft.secret.server.backend.sql.repository;

import org.apache.airavata.mft.secret.server.backend.sql.entity.S3SecretEntity;
import org.apache.airavata.mft.secret.server.backend.sql.entity.SCPSecretEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface S3SecretRepository extends CrudRepository<S3SecretEntity, String> {
    Optional<S3SecretEntity> findBySecretId(String secretId);
}
