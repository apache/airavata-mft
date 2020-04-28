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

package org.apache.airavata.mft.secret.server.backend;

import org.apache.airavata.mft.secret.service.*;

import java.util.Optional;

public interface SecretBackend {

    void init();
    void destroy();

    Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception;
    SCPSecret createSCPSecret(SCPSecretCreateRequest request);
    boolean updateSCPSecret(SCPSecretUpdateRequest request);
    boolean deleteSCPSecret(SCPSecretDeleteRequest request);

    Optional<S3Secret> getS3Secret(S3SecretGetRequest request) throws Exception;
    S3Secret createS3Secret(S3SecretCreateRequest request) throws Exception;
    boolean updateS3Secret(S3SecretUpdateRequest request) throws Exception;
    boolean deleteS3Secret(S3SecretDeleteRequest request) throws Exception;

    Optional<BoxSecret> getBoxSecret(BoxSecretGetRequest request) throws Exception;
    BoxSecret createBoxSecret(BoxSecretCreateRequest request) throws Exception;
    boolean updateBoxSecret(BoxSecretUpdateRequest request) throws Exception;
    boolean deleteBoxSecret(BoxSecretDeleteRequest request) throws Exception;

    Optional<AzureSecret> getAzureSecret(AzureSecretGetRequest request) throws Exception;
    AzureSecret createAzureSecret(AzureSecretCreateRequest request) throws Exception;
    boolean updateAzureSecret(AzureSecretUpdateRequest request) throws Exception;
    boolean deleteAzureSecret(AzureSecretDeleteRequest request) throws Exception;

    Optional<GCSSecret> getGCSSecret(GCSSecretGetRequest request) throws Exception;
    GCSSecret createGCSSecret(GCSSecretCreateRequest request) throws Exception;
    boolean updateGCSSecret(GCSSecretUpdateRequest request) throws Exception;
    boolean deleteGCSSecret(GCSSecretDeleteRequest request) throws Exception;

    Optional<FTPSecret> getFTPSecret(FTPSecretGetRequest request) throws Exception;
    FTPSecret createFTPSecret(FTPSecretCreateRequest request) throws Exception;
    boolean updateFTPSecret(FTPSecretUpdateRequest request) throws Exception;
    boolean deleteFTPSecret(FTPSecretDeleteRequest request) throws Exception;

    Optional<DropboxSecret> getDropboxSecret(DropboxSecretGetRequest request) throws Exception;
    DropboxSecret createDropboxSecret(DropboxSecretCreateRequest request) throws Exception;
    boolean updateDropboxSecret(DropboxSecretUpdateRequest request) throws Exception;
    boolean deleteDropboxSecret(DropboxSecretDeleteRequest request) throws Exception;
}
