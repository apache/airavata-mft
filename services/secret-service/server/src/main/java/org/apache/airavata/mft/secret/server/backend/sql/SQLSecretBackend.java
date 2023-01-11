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
import org.apache.airavata.mft.credential.stubs.odata.*;
import org.apache.airavata.mft.credential.stubs.s3.*;
import org.apache.airavata.mft.credential.stubs.scp.*;
import org.apache.airavata.mft.credential.stubs.swift.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.server.backend.sql.entity.*;
import org.apache.airavata.mft.secret.server.backend.sql.entity.swift.SwiftAuthCredentialSecretEntity;
import org.apache.airavata.mft.secret.server.backend.sql.entity.swift.SwiftPasswordSecretEntity;
import org.apache.airavata.mft.secret.server.backend.sql.entity.swift.SwiftSecretEntity;
import org.apache.airavata.mft.secret.server.backend.sql.repository.*;
import org.apache.airavata.mft.secret.server.backend.sql.repository.swift.SwiftAuthCredentialSecretRepository;
import org.apache.airavata.mft.secret.server.backend.sql.repository.swift.SwiftPasswordSecretRepository;
import org.apache.airavata.mft.secret.server.backend.sql.repository.swift.SwiftSecretRepository;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("SQLSecretBackend")
public class SQLSecretBackend implements SecretBackend {

    private static final Logger logger = LoggerFactory.getLogger(SQLSecretBackend.class);

    @Autowired
    private SCPSecretRepository scpSecretRepository;

    @Autowired
    private FTPSecretRepository ftpSecretRepository;

    @Autowired
    private S3SecretRepository s3SecretRepository;

    @Autowired
    private AzureSecretRepository azureSecretRepository;

    @Autowired
    private SwiftSecretRepository swiftSecretRepository;

    @Autowired
    private SwiftPasswordSecretRepository swiftPasswordSecretRepository;

    @Autowired
    private SwiftAuthCredentialSecretRepository swiftAuthCredentialSecretRepository;

    @Autowired
    private ODataSecretRepository odataSecretRepository;

    @Autowired
    private GCSSecretRepository gcsSecretRepository;

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
        Optional<AzureSecretEntity> secretEty = azureSecretRepository.findBySecretId(request.getSecretId());
        return secretEty.map(azSecretEntity -> mapper.map(azSecretEntity, AzureSecret.newBuilder().getClass()).build());
    }

    @Override
    public AzureSecret createAzureSecret(AzureSecretCreateRequest request) throws Exception {
        AzureSecretEntity savedEntity = azureSecretRepository.save(mapper.map(request, AzureSecretEntity.class));
        return mapper.map(savedEntity, AzureSecret.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateAzureSecret(AzureSecretUpdateRequest request) throws Exception {
        azureSecretRepository.save(mapper.map(request, AzureSecretEntity.class));
        return true;
    }

    @Override
    public boolean deleteAzureSecret(AzureSecretDeleteRequest request) throws Exception {
        azureSecretRepository.deleteById(request.getSecretId());
        return true;
    }

    @Override
    public Optional<GCSSecret> getGCSSecret( GCSSecretGetRequest request ) throws Exception
    {
        Optional<GCSSecretEntity> secretEty = gcsSecretRepository.findBySecretId( request.getSecretId() );
        return secretEty.map( gcsSecretEntity -> mapper.map( gcsSecretEntity, GCSSecret.newBuilder().getClass() ).build() );
    }

    @Override
    public GCSSecret createGCSSecret(GCSSecretCreateRequest request) throws Exception {
        GCSSecretEntity savedEntity = gcsSecretRepository.save(mapper.map(request, GCSSecretEntity.class));
        return mapper.map(savedEntity, GCSSecret.newBuilder().getClass()).build();
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
    public Optional<SwiftSecret> getSwiftSecret(SwiftSecretGetRequest request) throws Exception {
        Optional<SwiftSecretEntity> secEtyOp = swiftSecretRepository.findBySecretId(request.getSecretId());
        if (secEtyOp.isPresent()) {
            SwiftSecret.Builder secBuilder = SwiftSecret.newBuilder();
            SwiftSecretEntity secEty = secEtyOp.get();
            secBuilder.setSecretId(secEty.getSecretId());

            switch (secEty.getInternalSecretType()) {
                case PASSWORD:
                    Optional<SwiftPasswordSecretEntity> passSec = swiftPasswordSecretRepository
                            .findBySecretId(secEty.getInternalSecretId());
                    if (passSec.isPresent()) {
                        SwiftPasswordSecret.Builder passBuilder = SwiftPasswordSecret.newBuilder();
                        mapper.map(passSec.get(), passBuilder);
                        secBuilder.setPasswordSecret(passBuilder.build());
                    } else {
                        throw new Exception("Can not find a swift password secret with id " + secEty.getInternalSecretId());
                    }
                    break;
                case AUTH_CREDENTIAL:
                    Optional<SwiftAuthCredentialSecretEntity> authCredSec = swiftAuthCredentialSecretRepository
                            .findBySecretId(secEty.getInternalSecretId());
                    if (authCredSec.isPresent()) {
                        SwiftAuthCredentialSecret.Builder authBuilder = SwiftAuthCredentialSecret.newBuilder();
                        mapper.map(authCredSec.get(), authBuilder);
                        secBuilder.setAuthCredentialSecret(authBuilder.build());
                    } else {
                        throw new Exception("Can not find a swift auth cred secret with id " + secEty.getInternalSecretId());
                    }
                    break;
                default:
                    throw new Exception("Non compatible internal secret type : " + secEty.getInternalSecretType());
            }
            return Optional.of(secBuilder.build());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public SwiftSecret createSwiftSecret(SwiftSecretCreateRequest request) throws Exception {

        SwiftSecretEntity secEty = new SwiftSecretEntity();
        SwiftAuthCredentialSecretEntity authCredSaved = null;
        SwiftPasswordSecretEntity passSecSaved = null;

        switch (request.getSecretCase()) {
            case PASSWORDSECRET:
                passSecSaved = swiftPasswordSecretRepository
                        .save(mapper.map(request.getPasswordSecret(), SwiftPasswordSecretEntity.class));
                secEty.setInternalSecretId(passSecSaved.getSecretId());
                secEty.setInternalSecretType(SwiftSecretEntity.InternalSecretType.PASSWORD);
                break;
            case AUTHCREDENTIALSECRET:
                authCredSaved = swiftAuthCredentialSecretRepository
                        .save(mapper.map(request.getAuthCredentialSecret(), SwiftAuthCredentialSecretEntity.class));
                secEty.setInternalSecretId(authCredSaved.getSecretId());
                secEty.setInternalSecretType(SwiftSecretEntity.InternalSecretType.AUTH_CREDENTIAL);
                break;
            case SECRET_NOT_SET:
                throw new Exception("No internal secret is set");
        }

        SwiftSecretEntity savedEty = swiftSecretRepository.save(secEty);
        SwiftSecret.Builder secBuilder = SwiftSecret.newBuilder();
        secBuilder.setSecretId(savedEty.getSecretId());
        switch (savedEty.getInternalSecretType()) {
            case PASSWORD:
                secBuilder.setPasswordSecret(mapper.map(passSecSaved, SwiftPasswordSecret.newBuilder().getClass()));
                break;
            case AUTH_CREDENTIAL:
                secBuilder.setAuthCredentialSecret(mapper.map(authCredSaved, SwiftAuthCredentialSecret.newBuilder().getClass()));
                break;
        }

        return secBuilder.build();
    }

    @Override
    public boolean updateSwiftSecret(SwiftSecretUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteSwiftSecret(SwiftSecretDeleteRequest request) throws Exception {
        Optional<SwiftSecretEntity> secOp = swiftSecretRepository.findBySecretId(request.getSecretId());
        if (secOp.isPresent()) {
            swiftSecretRepository.deleteById(request.getSecretId());
            switch (secOp.get().getInternalSecretType()) {
                case AUTH_CREDENTIAL:
                    swiftAuthCredentialSecretRepository.deleteById(secOp.get().getInternalSecretId());
                    break;
                case PASSWORD:
                    swiftPasswordSecretRepository.deleteById(secOp.get().getInternalSecretId());
                    break;
            }
            return true;
        } else {
            return false;
        }
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

    @Override
    public Optional<ODataSecret> getODataSecret(ODataSecretGetRequest request) throws Exception {
        Optional<ODataSecretEntity> secretEty = odataSecretRepository.findBySecretId(request.getSecretId());
        return secretEty.map(odataSecretEntity -> mapper.map(odataSecretEntity, ODataSecret.newBuilder().getClass()).build());
    }

    @Override
    public ODataSecret createODataSecret(ODataSecretCreateRequest request) throws Exception {
        ODataSecretEntity savedEntity = odataSecretRepository.save(mapper.map(request, ODataSecretEntity.class));
        return mapper.map(savedEntity, ODataSecret.newBuilder().getClass()).build();
    }

    @Override
    public boolean updateODataSecret(ODataSecretUpdateRequest request) throws Exception {
        odataSecretRepository.save(mapper.map(request, ODataSecretEntity.class));
        return true;
    }

    @Override
    public boolean deleteODataSecret(ODataSecretDeleteRequest request) throws Exception {
        odataSecretRepository.deleteById(request.getSecretId());
        return true;
    }
}
