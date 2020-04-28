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

package org.apache.airavata.mft.resource.server.backend;

import org.apache.airavata.mft.resource.service.*;

import java.util.Optional;

public interface ResourceBackend {

    void init();
    void destroy();

    Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) throws Exception;
    SCPStorage createSCPStorage(SCPStorageCreateRequest request) throws Exception;
    boolean updateSCPStorage(SCPStorageUpdateRequest request) throws Exception;
    boolean deleteSCPStorage(SCPStorageDeleteRequest request) throws Exception;

    Optional<SCPResource> getSCPResource(SCPResourceGetRequest request) throws Exception;
    SCPResource createSCPResource(SCPResourceCreateRequest request) throws Exception;
    boolean updateSCPResource(SCPResourceUpdateRequest request) throws Exception;
    boolean deleteSCPResource(SCPResourceDeleteRequest request) throws Exception;

    Optional<LocalResource> getLocalResource(LocalResourceGetRequest request) throws Exception;
    LocalResource createLocalResource(LocalResourceCreateRequest request) throws Exception;
    boolean updateLocalResource(LocalResourceUpdateRequest request) throws Exception;
    boolean deleteLocalResource(LocalResourceDeleteRequest request) throws Exception;

    Optional<S3Resource> getS3Resource(S3ResourceGetRequest request) throws Exception;
    S3Resource createS3Resource(S3ResourceCreateRequest request) throws Exception;
    boolean updateS3Resource(S3ResourceUpdateRequest request) throws Exception;
    boolean deleteS3Resource(S3ResourceDeleteRequest request) throws Exception;

    Optional<BoxResource> getBoxResource(BoxResourceGetRequest request) throws Exception;
    BoxResource createBoxResource(BoxResourceCreateRequest request) throws Exception;
    boolean updateBoxResource(BoxResourceUpdateRequest request) throws Exception;
    boolean deleteBoxResource(BoxResourceDeleteRequest request) throws Exception;

    Optional<AzureResource> getAzureResource(AzureResourceGetRequest request) throws Exception;
    AzureResource createAzureResource(AzureResourceCreateRequest request) throws Exception;
    boolean updateAzureResource(AzureResourceUpdateRequest request) throws Exception;
    boolean deleteAzureResource(AzureResourceDeleteRequest request) throws Exception;

    Optional<GCSResource> getGCSResource(GCSResourceGetRequest request) throws Exception;
    GCSResource createGCSResource(GCSResourceCreateRequest request) throws Exception;
    boolean updateGCSResource(GCSResourceUpdateRequest request) throws Exception;
    boolean deleteGCSResource(GCSResourceDeleteRequest request) throws Exception;

    Optional<FTPResource> getFTPResource(FTPResourceGetRequest request) throws Exception;
    FTPResource createFTPResource(FTPResourceCreateRequest request) throws Exception;
    boolean updateFTPResource(FTPResourceUpdateRequest request) throws Exception;
    boolean deleteFTPResource(FTPResourceDeleteRequest request) throws Exception;

    Optional<FTPStorage> getFTPStorage(FTPStorageGetRequest request) throws Exception;
    FTPStorage createFTPStorage(FTPStorageCreateRequest request) throws Exception;
    boolean updateFTPStorage(FTPStorageUpdateRequest request) throws Exception;
    boolean deleteFTPStorage(FTPStorageDeleteRequest request) throws Exception;

    Optional<DropboxResource> getDropboxResource(DropboxResourceGetRequest request) throws Exception;
    DropboxResource createDropboxResource(DropboxResourceCreateRequest request) throws Exception;
    boolean updateDropboxResource(DropboxResourceUpdateRequest request) throws Exception;
    boolean deleteDropboxResource(DropboxResourceDeleteRequest request) throws Exception;
}
