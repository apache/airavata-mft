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

import org.apache.airavata.mft.credential.stubs.azure.*;
import org.apache.airavata.mft.credential.stubs.box.*;
import org.apache.airavata.mft.credential.stubs.dropbox.*;
import org.apache.airavata.mft.credential.stubs.ftp.*;
import org.apache.airavata.mft.credential.stubs.gcs.*;
import org.apache.airavata.mft.credential.stubs.http.*;
import org.apache.airavata.mft.credential.stubs.odata.*;
import org.apache.airavata.mft.credential.stubs.s3.*;
import org.apache.airavata.mft.credential.stubs.scp.*;
import org.apache.airavata.mft.credential.stubs.swift.*;

import java.util.Optional;

public interface SecretBackend {

    public void init();
    public void destroy();

    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception;
    public SCPSecret createSCPSecret(SCPSecretCreateRequest request);
    public boolean updateSCPSecret(SCPSecretUpdateRequest request);
    public boolean deleteSCPSecret(SCPSecretDeleteRequest request);

    public Optional<S3Secret> getS3Secret(S3SecretGetRequest request) throws Exception;
    public S3Secret createS3Secret(S3SecretCreateRequest request) throws Exception;
    public boolean updateS3Secret(S3SecretUpdateRequest request) throws Exception;
    public boolean deleteS3Secret(S3SecretDeleteRequest request) throws Exception;

    public Optional<BoxSecret> getBoxSecret(BoxSecretGetRequest request) throws Exception;
    public BoxSecret createBoxSecret(BoxSecretCreateRequest request) throws Exception;
    public boolean updateBoxSecret(BoxSecretUpdateRequest request) throws Exception;
    public boolean deleteBoxSecret(BoxSecretDeleteRequest request) throws Exception;

    public Optional<AzureSecret> getAzureSecret(AzureSecretGetRequest request) throws Exception;
    public AzureSecret createAzureSecret(AzureSecretCreateRequest request) throws Exception;
    public boolean updateAzureSecret(AzureSecretUpdateRequest request) throws Exception;
    public boolean deleteAzureSecret(AzureSecretDeleteRequest request) throws Exception;

    public Optional<GCSSecret> getGCSSecret(GCSSecretGetRequest request) throws Exception;
    public GCSSecret createGCSSecret(GCSSecretCreateRequest request) throws Exception;
    public boolean updateGCSSecret(GCSSecretUpdateRequest request) throws Exception;
    public boolean deleteGCSSecret(GCSSecretDeleteRequest request) throws Exception;

    public Optional<DropboxSecret> getDropboxSecret(DropboxSecretGetRequest request) throws Exception;
    public DropboxSecret createDropboxSecret(DropboxSecretCreateRequest request) throws Exception;
    public boolean updateDropboxSecret(DropboxSecretUpdateRequest request) throws Exception;
    public boolean deleteDropboxSecret(DropboxSecretDeleteRequest request) throws Exception;

    Optional<FTPSecret> getFTPSecret(FTPSecretGetRequest request) throws Exception;
    FTPSecret createFTPSecret(FTPSecretCreateRequest request) throws Exception;
    boolean updateFTPSecret(FTPSecretUpdateRequest request) throws Exception;
    boolean deleteFTPSecret(FTPSecretDeleteRequest request) throws Exception;

    Optional<SwiftSecret> getSwiftSecret(SwiftSecretGetRequest request) throws Exception;
    SwiftSecret createSwiftSecret(SwiftSecretCreateRequest request) throws Exception;
    boolean updateSwiftSecret(SwiftSecretUpdateRequest request) throws Exception;
    boolean deleteSwiftSecret(SwiftSecretDeleteRequest request) throws Exception;

    Optional<ODataSecret> getODataSecret(ODataSecretGetRequest request) throws Exception;
    ODataSecret createODataSecret(ODataSecretCreateRequest request) throws Exception;
    boolean updateODataSecret(ODataSecretUpdateRequest request) throws Exception;
    boolean deleteODataSecret(ODataSecretDeleteRequest request) throws Exception;

    Optional<HTTPSecret> getHttpSecret(HTTPSecretGetRequest request) throws Exception;
    HTTPSecret createHttpSecret(HTTPSecretCreateRequest request) throws Exception;
    boolean updateHttpSecret(HTTPSecretUpdateRequest request) throws Exception;
    boolean deleteHttpSecret(HTTPSecretDeleteRequest request) throws Exception;
}
