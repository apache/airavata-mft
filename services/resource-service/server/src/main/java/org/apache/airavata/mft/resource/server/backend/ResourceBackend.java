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

import org.apache.airavata.mft.resource.stubs.azure.resource.*;
import org.apache.airavata.mft.resource.stubs.azure.storage.*;
import org.apache.airavata.mft.resource.stubs.box.resource.*;
import org.apache.airavata.mft.resource.stubs.box.storage.*;
import org.apache.airavata.mft.resource.stubs.dropbox.resource.*;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.*;
import org.apache.airavata.mft.resource.stubs.ftp.resource.*;
import org.apache.airavata.mft.resource.stubs.ftp.storage.*;
import org.apache.airavata.mft.resource.stubs.gcs.resource.*;
import org.apache.airavata.mft.resource.stubs.gcs.storage.*;
import org.apache.airavata.mft.resource.stubs.local.resource.*;
import org.apache.airavata.mft.resource.stubs.local.storage.*;
import org.apache.airavata.mft.resource.stubs.s3.resource.*;
import org.apache.airavata.mft.resource.stubs.s3.storage.*;
import org.apache.airavata.mft.resource.stubs.scp.resource.*;
import org.apache.airavata.mft.resource.stubs.scp.storage.*;

import java.util.Optional;

public interface ResourceBackend {

    public void init();
    public void destroy();

    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) throws Exception;
    public SCPStorage createSCPStorage(SCPStorageCreateRequest request) throws Exception;
    public boolean updateSCPStorage(SCPStorageUpdateRequest request) throws Exception;
    public boolean deleteSCPStorage(SCPStorageDeleteRequest request) throws Exception;

    public Optional<SCPResource> getSCPResource(SCPResourceGetRequest request) throws Exception;
    public SCPResource createSCPResource(SCPResourceCreateRequest request) throws Exception;
    public boolean updateSCPResource(SCPResourceUpdateRequest request) throws Exception;
    public boolean deleteSCPResource(SCPResourceDeleteRequest request) throws Exception;

    public Optional<LocalStorage> getLocalStorage(LocalStorageGetRequest request) throws Exception;
    public LocalStorage createLocalStorage(LocalStorageCreateRequest request) throws Exception;
    public boolean updateLocalStorage(LocalStorageUpdateRequest request) throws Exception;
    public boolean deleteLocalStorage(LocalStorageDeleteRequest request) throws Exception;

    public Optional<LocalResource> getLocalResource(LocalResourceGetRequest request) throws Exception;
    public LocalResource createLocalResource(LocalResourceCreateRequest request) throws Exception;
    public boolean updateLocalResource(LocalResourceUpdateRequest request) throws Exception;
    public boolean deleteLocalResource(LocalResourceDeleteRequest request) throws Exception;

    public Optional<S3Storage> getS3Storage(S3StorageGetRequest request) throws Exception;
    public S3Storage createS3Storage(S3StorageCreateRequest request) throws Exception;
    public boolean updateS3Storage(S3StorageUpdateRequest request) throws Exception;
    public boolean deleteS3Storage(S3StorageDeleteRequest request) throws Exception;

    public Optional<S3Resource> getS3Resource(S3ResourceGetRequest request) throws Exception;
    public S3Resource createS3Resource(S3ResourceCreateRequest request) throws Exception;
    public boolean updateS3Resource(S3ResourceUpdateRequest request) throws Exception;
    public boolean deleteS3Resource(S3ResourceDeleteRequest request) throws Exception;

    public Optional<BoxStorage> getBoxStorage(BoxStorageGetRequest request) throws Exception;
    public BoxStorage createBoxStorage(BoxStorageCreateRequest request) throws Exception;
    public boolean updateBoxStorage(BoxStorageUpdateRequest request) throws Exception;
    public boolean deleteBoxStorage(BoxStorageDeleteRequest request) throws Exception;

    public Optional<BoxResource> getBoxResource(BoxResourceGetRequest request) throws Exception;
    public BoxResource createBoxResource(BoxResourceCreateRequest request) throws Exception;
    public boolean updateBoxResource(BoxResourceUpdateRequest request) throws Exception;
    public boolean deleteBoxResource(BoxResourceDeleteRequest request) throws Exception;

    public Optional<AzureStorage> getAzureStorage(AzureStorageGetRequest request) throws Exception;
    public AzureStorage createAzureStorage(AzureStorageCreateRequest request) throws Exception;
    public boolean updateAzureStorage(AzureStorageUpdateRequest request) throws Exception;
    public boolean deleteAzureStorage(AzureStorageDeleteRequest request) throws Exception;

    public Optional<AzureResource> getAzureResource(AzureResourceGetRequest request) throws Exception;
    public AzureResource createAzureResource(AzureResourceCreateRequest request) throws Exception;
    public boolean updateAzureResource(AzureResourceUpdateRequest request) throws Exception;
    public boolean deleteAzureResource(AzureResourceDeleteRequest request) throws Exception;

    public Optional<GCSStorage> getGCSStorage(GCSStorageGetRequest request) throws Exception;
    public GCSStorage createGCSStorage(GCSStorageCreateRequest request) throws Exception;
    public boolean updateGCSStorage(GCSStorageUpdateRequest request) throws Exception;
    public boolean deleteGCSStorage(GCSStorageDeleteRequest request) throws Exception;

    public Optional<GCSResource> getGCSResource(GCSResourceGetRequest request) throws Exception;
    public GCSResource createGCSResource(GCSResourceCreateRequest request) throws Exception;
    public boolean updateGCSResource(GCSResourceUpdateRequest request) throws Exception;
    public boolean deleteGCSResource(GCSResourceDeleteRequest request) throws Exception;

    public Optional<DropboxStorage> getDropboxStorage(DropboxStorageGetRequest request) throws Exception;
    public DropboxStorage createDropboxStorage(DropboxStorageCreateRequest request) throws Exception;
    public boolean updateDropboxStorage(DropboxStorageUpdateRequest request) throws Exception;
    public boolean deleteDropboxStorage(DropboxStorageDeleteRequest request) throws Exception;

    public Optional<DropboxResource> getDropboxResource(DropboxResourceGetRequest request) throws Exception;
    public DropboxResource createDropboxResource(DropboxResourceCreateRequest request) throws Exception;
    public boolean updateDropboxResource(DropboxResourceUpdateRequest request) throws Exception;
    public boolean deleteDropboxResource(DropboxResourceDeleteRequest request) throws Exception;

    Optional<FTPStorage> getFTPStorage(FTPStorageGetRequest request) throws Exception;
    FTPStorage createFTPStorage(FTPStorageCreateRequest request) throws Exception;
    boolean updateFTPStorage(FTPStorageUpdateRequest request) throws Exception;
    boolean deleteFTPStorage(FTPStorageDeleteRequest request) throws Exception;

    Optional<FTPResource> getFTPResource(FTPResourceGetRequest request) throws Exception;
    FTPResource createFTPResource(FTPResourceCreateRequest request) throws Exception;
    boolean updateFTPResource(FTPResourceUpdateRequest request) throws Exception;
    boolean deleteFTPResource(FTPResourceDeleteRequest request) throws Exception;
}
