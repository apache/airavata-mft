/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.secret.server.backend.sql;

import org.apache.airavata.mft.credential.stubs.azure.*;
import org.apache.airavata.mft.credential.stubs.box.*;
import org.apache.airavata.mft.credential.stubs.dropbox.*;
import org.apache.airavata.mft.credential.stubs.ftp.*;
import org.apache.airavata.mft.credential.stubs.gcs.*;
import org.apache.airavata.mft.credential.stubs.s3.*;
import org.apache.airavata.mft.credential.stubs.scp.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.server.backend.sql.entity.FTPSecretEntity;
import org.apache.airavata.mft.secret.server.backend.sql.entity.S3SecretEntity;
import org.apache.airavata.mft.secret.server.backend.sql.entity.SCPSecretEntity;
import org.apache.airavata.mft.secret.server.backend.sql.repository.FTPSecretRepository;
import org.apache.airavata.mft.secret.server.backend.sql.repository.S3SecretRepository;
import org.apache.airavata.mft.secret.server.backend.sql.repository.SCPSecretRepository;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class SQLSecretBackend implements SecretBackend {

    private static final Logger logger = LoggerFactory.getLogger(SQLSecretBackend.class);

    @Autowired
    private SCPSecretRepository scpSecretRepository;

    @Autowired
    private FTPSecretRepository ftpSecretRepository;

    @Autowired
    private S3SecretRepository s3SecretRepository;

    private DozerBeanMapper mapper = new DozerBeanMapper();

    @Override
    public void init() {
        logger.info("Initializing database secret backend");
    }

    @Override
    public void destroy() {
        logger.info("Destroying database secret backend");
    }

    @Override
    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) {
        Optional<SCPSecretEntity> secretEty = scpSecretRepository.findBySecretId(request.getSecretId());
        return secretEty.map(scpSecretEntity -> mapper.map(scpSecretEntity, SCPSecret.newBuilder().getClass()).build());
    }

    @Override
    public SCPSecret createSCPSecret(SCPSecretCreateRequest request) {
        SCPSecretEntity savedEntity = scpSecretRepository.save(mapper.map(request, SCPSecretEntity.class));
        return mapper.map(savedEntity, SCPSecret.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateSCPSecret(SCPSecretUpdateRequest request) {
        scpSecretRepository.save(mapper.map(request, SCPSecretEntity.class));
        return true;
    }

    @Override
    public boolean deleteSCPSecret(SCPSecretDeleteRequest request) {
        scpSecretRepository.deleteById(request.getSecretId());
        return true;
    }

    @Override
    public Optional<S3Secret> getS3Secret(S3SecretGetRequest request) throws Exception {
        Optional<S3SecretEntity> secretEty = s3SecretRepository.findBySecretId(request.getSecretId());
        return secretEty.map(s3SecretEntity -> mapper.map(s3SecretEntity, S3Secret.newBuilder().getClass()).build());
    }

    @Override
    public S3Secret createS3Secret(S3SecretCreateRequest request) throws Exception {
        S3SecretEntity savedEntity = s3SecretRepository.save(mapper.map(request, S3SecretEntity.class));
        return mapper.map(savedEntity, S3Secret.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateS3Secret(S3SecretUpdateRequest request) throws Exception {
        s3SecretRepository.save(mapper.map(request, S3SecretEntity.class));
        return true;
    }

    @Override
    public boolean deleteS3Secret(S3SecretDeleteRequest request) throws Exception {
        s3SecretRepository.deleteById(request.getSecretId());
        return true;
    }

    @Override
    public Optional<BoxSecret> getBoxSecret(BoxSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public BoxSecret createBoxSecret(BoxSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateBoxSecret(BoxSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteBoxSecret(BoxSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<AzureSecret> getAzureSecret(AzureSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public AzureSecret createAzureSecret(AzureSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateAzureSecret(AzureSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteAzureSecret(AzureSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<GCSSecret> getGCSSecret(GCSSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GCSSecret createGCSSecret(GCSSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSSecret(GCSSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSSecret(GCSSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<DropboxSecret> getDropboxSecret(DropboxSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public DropboxSecret createDropboxSecret(DropboxSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateDropboxSecret(DropboxSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteDropboxSecret(DropboxSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<FTPSecret> getFTPSecret(FTPSecretGetRequest request) {
        Optional<FTPSecretEntity> secretEty = ftpSecretRepository.findBySecretId(request.getSecretId());
        return secretEty.map(ftpSecretEntity -> mapper.map(ftpSecretEntity, FTPSecret.newBuilder().getClass()).build());
    }

    @Override
    public FTPSecret createFTPSecret(FTPSecretCreateRequest request) {
        FTPSecretEntity savedEntity = ftpSecretRepository.save(mapper.map(request, FTPSecretEntity.class));
        return mapper.map(savedEntity, FTPSecret.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateFTPSecret(FTPSecretUpdateRequest request) {
        ftpSecretRepository.save(mapper.map(request, FTPSecretEntity.class));
        return true;
    }

    @Override
    public boolean deleteFTPSecret(FTPSecretDeleteRequest request) {
        ftpSecretRepository.deleteById(request.getSecretId());
        return true;
    }
}
