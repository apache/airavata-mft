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
import org.apache.airavata.registry.api.exception.RegistryServiceException;

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

    public Optional<LocalResource> getLocalResource(LocalResourceGetRequest request) throws Exception;
    public LocalResource createLocalResource(LocalResourceCreateRequest request) throws Exception;
    public boolean updateLocalResource(LocalResourceUpdateRequest request) throws Exception;
    public boolean deleteLocalResource(LocalResourceDeleteRequest request) throws Exception;

    public Optional<S3Resource> getS3Resource(S3ResourceGetRequest request) throws Exception;
    public S3Resource createS3Resource(S3ResourceCreateRequest request) throws Exception;
    public boolean updateS3Resource(S3ResourceUpdateRequest request) throws Exception;
    public boolean deleteS3Resource(S3ResourceDeleteRequest request) throws Exception;

    public Optional<BoxResource> getBoxResource(BoxResourceGetRequest request) throws Exception;
    public BoxResource createBoxResource(BoxResourceCreateRequest request) throws Exception;
    public boolean updateBoxResource(BoxResourceUpdateRequest request) throws Exception;
    public boolean deleteBoxResource(BoxResourceDeleteRequest request) throws Exception;

    public Optional<AzureResource> getAzureResource(AzureResourceGetRequest request) throws Exception;
    public AzureResource createAzureResource(AzureResourceCreateRequest request) throws Exception;
    public boolean updateAzureResource(AzureResourceUpdateRequest request) throws Exception;
    public boolean deleteAzureResource(AzureResourceDeleteRequest request) throws Exception;

    public Optional<GCSResource> getGCSResource(GCSResourceGetRequest request) throws Exception;
    public GCSResource createGCSResource(GCSResourceCreateRequest request) throws Exception;
    public boolean updateGCSResource(GCSResourceUpdateRequest request) throws Exception;
    public boolean deleteGCSResource(GCSResourceDeleteRequest request) throws Exception;

    public Optional<DropboxResource> getDropboxResource(DropboxResourceGetRequest request) throws Exception;
    public DropboxResource createDropboxResource(DropboxResourceCreateRequest request) throws Exception;
    public boolean updateDropboxResource(DropboxResourceUpdateRequest request) throws Exception;
    public boolean deleteDropboxResource(DropboxResourceDeleteRequest request) throws Exception;
}
