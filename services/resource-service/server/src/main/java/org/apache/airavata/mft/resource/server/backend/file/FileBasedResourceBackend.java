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

 package org.apache.airavata.mft.resource.server.backend.file;

import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.stubs.azure.storage.*;
import org.apache.airavata.mft.resource.stubs.box.storage.*;
import org.apache.airavata.mft.resource.stubs.common.*;
import org.apache.airavata.mft.resource.stubs.dropbox.storage.*;
import org.apache.airavata.mft.resource.stubs.ftp.storage.*;
import org.apache.airavata.mft.resource.stubs.gcs.storage.*;
import org.apache.airavata.mft.resource.stubs.local.storage.*;
import org.apache.airavata.mft.resource.stubs.s3.storage.*;
import org.apache.airavata.mft.resource.stubs.scp.storage.*;
import org.apache.airavata.mft.storage.stubs.storagesecret.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class FileBasedResourceBackend implements ResourceBackend {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedResourceBackend.class);

    @org.springframework.beans.factory.annotation.Value("${file.backend.resource.file}")
    private String resourceFile;

    @org.springframework.beans.factory.annotation.Value("${file.backend.storage.file}")
    private String storageFile;

    @Override
    public void init() {
        logger.info("Initializing file based resource backend");
    }

    @Override
    public void destroy() {
        logger.info("Destroying file based resource backend");
    }

    @Override
    public Optional<GenericResource> getGenericResource(GenericResourceGetRequest request) throws Exception {
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream("resources.json");

        JSONParser jsonParser = new JSONParser();

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {

            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            return resourceList.stream()
                    .filter(resource -> request.getResourceId().equals(((JSONObject) resource).get("resourceId").toString()))
                    .findFirst().map(resource -> {
                JSONObject r = (JSONObject) resource;

                GenericResource.Builder resourceBuilder = GenericResource.newBuilder();
                String resourcePath = r.get("resourcePath").toString();

                resourceBuilder.setResourceId(request.getResourceId());

                switch (r.get("resourceMode").toString()) {
                    case "FILE":
                        FileResource fileResource = FileResource.newBuilder().setResourcePath(resourcePath).build();
                        resourceBuilder.setFile(fileResource);
                        break;
                    case "DIRECTORY":
                        DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(resourcePath).build();
                        resourceBuilder.setDirectory(directoryResource);
                        break;
                }

                String storageId = r.get("storageId").toString();
                String type = r.get("type").toString();

                try {
                    switch (type) {
                        case "SCP":
                            resourceBuilder.setScpStorage(getSCPStorage(SCPStorageGetRequest.newBuilder()
                                    .setStorageId(storageId).build()).orElseThrow(() -> new Exception("Storage not found")));
                            break;
                        case "S3":
                            resourceBuilder.setS3Storage(getS3Storage(S3StorageGetRequest.newBuilder()
                                    .setStorageId(storageId).build()).orElseThrow(() -> new Exception("Storage not found")));
                            break;
                        case "LOCAL":
                            resourceBuilder.setLocalStorage(getLocalStorage(LocalStorageGetRequest.newBuilder()
                                    .setStorageId(storageId).build()).orElseThrow(() -> new Exception("Storage not found")));
                            break;
                        case "DROPBOX":
                            resourceBuilder.setDropboxStorage(getDropboxStorage(DropboxStorageGetRequest.newBuilder()
                                    .setStorageId(storageId).build()).orElseThrow(() -> new Exception("Storage not found")));
                            break;
                        case "FTP":
                            resourceBuilder.setFtpStorage(getFTPStorage(FTPStorageGetRequest.newBuilder()
                                    .setStorageId(storageId).build()).orElseThrow(() -> new Exception("Storage not found")));
                            break;
                        case "BOX":
                            resourceBuilder.setBoxStorage(getBoxStorage(BoxStorageGetRequest.newBuilder()
                                    .setStorageId(storageId).build()).orElseThrow(() -> new Exception("Storage not found")));
                            break;
                        case "GCS":
                            resourceBuilder.setGcsStorage(getGCSStorage(GCSStorageGetRequest.newBuilder()
                                    .setStorageId(storageId).build()).orElseThrow(() -> new Exception("Storage not found")));
                            break;

                    }
                } catch (Exception e) {
                    logger.error("Failed to fetch storage with id {} and type {}", storageId, type);
                    return null;
                }
                return resourceBuilder.build();

            });
        }
    }

    @Override
    public GenericResource createGenericResource(GenericResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGenericResource(GenericResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGenericResource(GenericResourceDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<StorageSecret> getStorageSecret(StorageSecretGetRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public StorageSecret createStorageSecret(StorageSecretCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateStorageSecret(StorageSecretUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteStorageSecret(StorageSecretDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<StorageSecret> searchStorageSecret(StorageSecretSearchRequest request) throws Exception {
        return Optional.empty();
    }

    @Override
    public SCPStorageListResponse listSCPStorage(SCPStorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) throws Exception {
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(storageFile);

        JSONParser jsonParser = new JSONParser();

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {

            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<SCPStorage> scpStorages = (List<SCPStorage>) resourceList.stream()
                    .filter(resource -> "SCP".equals(((JSONObject) resource).get("type").toString()))
                    .map(st -> {
                        JSONObject s = (JSONObject) st;

                        SCPStorage storage = SCPStorage.newBuilder()
                                .setStorageId(s.get("storageId").toString())
                                .setHost(s.get("host").toString())
                                .setPort(Integer.parseInt(s.get("port").toString())).build();

                        return storage;

                    }).collect(Collectors.toList());

            return scpStorages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public SCPStorage createSCPStorage(SCPStorageCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateSCPStorage(SCPStorageUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteSCPStorage(SCPStorageDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public LocalStorageListResponse listLocalStorage(LocalStorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<LocalStorage> getLocalStorage(LocalStorageGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(storageFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray storageList = (JSONArray) obj;

            List<LocalStorage> localStorages = (List<LocalStorage>) storageList.stream()
                    .filter(storage -> "LOCAL".equals(((JSONObject) storage).get("type").toString()))
                    .map( storage -> {
                        JSONObject s = (JSONObject) storage;

                        LocalStorage st = LocalStorage.newBuilder()
                                .setStorageId(s.get("storageId").toString())
                                .setAgentId(s.get("agentId").toString())
                                .build();

                        return st;
                    }).collect(Collectors.toList());
            return localStorages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public LocalStorage createLocalStorage(LocalStorageCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateLocalStorage(LocalStorageUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteLocalStorage(LocalStorageDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public S3StorageListResponse listS3Storage(S3StorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<S3Storage> getS3Storage(S3StorageGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(storageFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray storagesList = (JSONArray) obj;

            List<S3Storage> s3Storages = (List<S3Storage>) storagesList.stream()
                    .filter(storage -> "S3".equals(((JSONObject) storage).get("type").toString()))
                    .map(storage -> {
                        JSONObject s = (JSONObject) storage;

                        S3Storage st = S3Storage.newBuilder()
                                .setStorageId(s.get("storageId").toString())
                                .setRegion(s.get("region").toString())
                                .setRegion(s.get("bucketName").toString())
                                .build();

                        return st;
                    }).collect(Collectors.toList());
            return s3Storages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public S3Storage createS3Storage(S3StorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateS3Storage(S3StorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteS3Storage(S3StorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public BoxStorageListResponse listBoxStorage(BoxStorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<BoxStorage> getBoxStorage(BoxStorageGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(storageFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray storageList = (JSONArray) obj;

            System.out.println("All resources ");
            List<BoxStorage> boxStorages = (List<BoxStorage>) storageList.stream()
                    .filter(storage -> "BOX".equals(((JSONObject) storage).get("type").toString()))
                    .map(storage -> {
                        JSONObject s = (JSONObject) storage;

                        BoxStorage st = BoxStorage.newBuilder()
                                .setStorageId(s.get("storageId").toString())
                                .build();

                        return st;
                    }).collect(Collectors.toList());
            return boxStorages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public BoxStorage createBoxStorage(BoxStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateBoxStorage(BoxStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteBoxStorage(BoxStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public AzureStorageListResponse listAzureStorage(AzureStorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<AzureStorage> getAzureStorage(AzureStorageGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(storageFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray storageList = (JSONArray) obj;

            List<AzureStorage> azureStorages = (List<AzureStorage>) storageList.stream()
                    .filter(storage -> "AZURE".equals(((JSONObject) storage).get("type").toString()))
                    .map(storage -> {
                        JSONObject s = (JSONObject) storage;

                        AzureStorage st = AzureStorage.newBuilder()
                                .setStorageId(s.get("storageId").toString())
                                .setContainer(s.get("container").toString())
                                .build();

                        return st;
                    }).collect(Collectors.toList());
            return azureStorages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public AzureStorage createAzureStorage(AzureStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateAzureStorage(AzureStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteAzureStorage(AzureStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public GCSStorageListResponse listGCSStorage(GCSStorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<GCSStorage> getGCSStorage(GCSStorageGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(storageFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray storageList = (JSONArray) obj;

            List<GCSStorage> gcsStorages = (List<GCSStorage>) storageList.stream()
                    .filter(storage -> "GCS".equals(((JSONObject) storage).get("type").toString()))
                    .map(storage -> {
                        JSONObject s = (JSONObject) storage;

                        GCSStorage st = GCSStorage.newBuilder()
                                .setStorageId(s.get("storageId").toString())
                                .setBucketName(s.get("bucketName").toString())
                                .build();

                        return st;
                    }).collect(Collectors.toList());
            return gcsStorages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public GCSStorage createGCSStorage(GCSStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSStorage(GCSStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSStorage(GCSStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public DropboxStorageListResponse listDropboxStorage(DropboxStorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<DropboxStorage> getDropboxStorage(DropboxStorageGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray storageList = (JSONArray) obj;

            List<DropboxStorage> dropboxStorages = (List<DropboxStorage>) storageList.stream()
                    .filter(resource -> "DROPBOX".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;
                        String resourcePath = r.get("resourcePath").toString();
                        resourcePath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;

                        DropboxStorage storage = DropboxStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("dropboxStorage")).get("storageId").toString())
                                .build();

                        return storage;
                    }).collect(Collectors.toList());
            return dropboxStorages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public DropboxStorage createDropboxStorage(DropboxStorageCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateDropboxStorage(DropboxStorageUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteDropboxStorage(DropboxStorageDeleteRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public FTPStorageListResponse listFTPStorage(FTPStorageListRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public Optional<FTPStorage> getFTPStorage(FTPStorageGetRequest request) throws Exception {
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(storageFile);

        JSONParser jsonParser = new JSONParser();

        if (inputStream == null) {
            throw new IOException("resources file not found");
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {

            Object obj = jsonParser.parse(reader);

            JSONArray storageList = (JSONArray) obj;

            List<FTPStorage> ftpStorages = (List<FTPStorage>) storageList.stream()
                    .filter(storage -> "FTP".equals(((JSONObject) storage).get("type").toString()))
                    .map(storage -> {
                        JSONObject s = (JSONObject) storage;

                        FTPStorage st = FTPStorage.newBuilder()
                                .setStorageId(s.get("storageId").toString())
                                .setHost(s.get("host").toString())
                                .setPort(Integer.parseInt(s.get("port").toString())).build();

                        return st;

                    }).collect(Collectors.toList());

            return ftpStorages.stream().filter(s -> request.getStorageId().equals(s.getStorageId())).findFirst();
        }
    }

    @Override
    public FTPStorage createFTPStorage(FTPStorageCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateFTPStorage(FTPStorageUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteFTPStorage(FTPStorageDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }
}
