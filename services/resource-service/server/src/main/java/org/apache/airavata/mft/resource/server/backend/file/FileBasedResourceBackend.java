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
import org.apache.airavata.mft.resource.stubs.azure.resource.*;
import org.apache.airavata.mft.resource.stubs.azure.storage.*;
import org.apache.airavata.mft.resource.stubs.box.resource.*;
import org.apache.airavata.mft.resource.stubs.box.storage.*;
import org.apache.airavata.mft.resource.stubs.common.DirectoryResource;
import org.apache.airavata.mft.resource.stubs.common.FileResource;
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
                                .setUser(s.get("user").toString())
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
    public Optional<SCPResource> getSCPResource(SCPResourceGetRequest request) throws Exception {

        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream("resources.json");

        JSONParser jsonParser = new JSONParser();

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {

            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<SCPResource> scpResources = (List<SCPResource>) resourceList.stream()
                    .filter(resource -> "SCP".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        SCPResource.Builder builder = SCPResource.newBuilder()
                                .setResourceId(r.get("resourceId").toString())
                                .setScpStorage(SCPStorage.newBuilder().setStorageId(r.get("storageId").toString()).getDefaultInstanceForType());

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();

                    }).collect(Collectors.toList());

            return scpResources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }
    }


    @Override
    public SCPResource createSCPResource(SCPResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateSCPResource(SCPResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteSCPResource(SCPResourceDeleteRequest request) {
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
    public Optional<LocalResource> getLocalResource(LocalResourceGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<LocalResource> localResources = (List<LocalResource>) resourceList.stream()
                    .filter(resource -> "LOCAL".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        LocalStorage storage = LocalStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("localStorage")).get("storageId").toString())
                                .setAgentId(((JSONObject)r.get("localStorage")).get("agentId").toString())
                                .build();

                        LocalResource.Builder builder = LocalResource.newBuilder()
                                .setLocalStorage(storage)
                                .setResourceId(r.get("resourceId").toString());

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();
                    }).collect(Collectors.toList());
            return localResources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }
    }

    @Override
    public LocalResource createLocalResource(LocalResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateLocalResource(LocalResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteLocalResource(LocalResourceDeleteRequest request) {
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
    public Optional<S3Resource> getS3Resource(S3ResourceGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<S3Resource> s3Resources = (List<S3Resource>) resourceList.stream()
                    .filter(resource -> "S3".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        S3Storage storage = S3Storage.newBuilder()
                                .setStorageId(((JSONObject)r.get("s3Storage")).get("storageId").toString())
                                .setRegion(((JSONObject)r.get("s3Storage")).get("region").toString())
                                .setRegion(((JSONObject)r.get("s3Storage")).get("bucketName").toString())
                                .build();

                        S3Resource.Builder builder = S3Resource.newBuilder()
                                .setResourceId(r.get("resourceId").toString())
                                .setS3Storage(storage);

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();
                    }).collect(Collectors.toList());
            return s3Resources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }

    }

    @Override
    public S3Resource createS3Resource(S3ResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean updateS3Resource(S3ResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");

    }

    @Override
    public boolean deleteS3Resource(S3ResourceDeleteRequest request) throws Exception {
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
    public Optional<BoxResource> getBoxResource(BoxResourceGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            System.out.println("All resources ");
            List<BoxResource> boxResources = (List<BoxResource>) resourceList.stream()
                    .filter(resource -> "BOX".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        BoxStorage storage = BoxStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("boxStorage")).get("storageId").toString())
                                .build();

                        BoxResource.Builder builder = BoxResource.newBuilder()
                                .setBoxStorage(storage)
                                .setResourceId(r.get("resourceId").toString());

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();
                    }).collect(Collectors.toList());
            return boxResources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }

    }

    @Override
    public BoxResource createBoxResource(BoxResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateBoxResource(BoxResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteBoxResource(BoxResourceDeleteRequest request) throws Exception {
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
    public Optional<AzureResource> getAzureResource(AzureResourceGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<AzureResource> azureResources = (List<AzureResource>) resourceList.stream()
                    .filter(resource -> "AZURE".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        AzureStorage storage = AzureStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("azureStorage")).get("storageId").toString())
                                .setContainer(((JSONObject)r.get("azureStorage")).get("container").toString())
                                .build();

                        AzureResource.Builder builder = AzureResource.newBuilder()
                                .setAzureStorage(storage)
                                .setResourceId(r.get("resourceId").toString());

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();

                    }).collect(Collectors.toList());
            return azureResources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }
    }

    @Override
    public AzureResource createAzureResource(AzureResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateAzureResource(AzureResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteAzureResource(AzureResourceDeleteRequest request) throws Exception {
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
    public Optional<GCSResource> getGCSResource(GCSResourceGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<GCSResource> gcsResources = (List<GCSResource>) resourceList.stream()
                    .filter(resource -> "GCS".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        GCSStorage storage = GCSStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("gcsStorage")).get("storageId").toString())
                                .setBucketName(((JSONObject)r.get("gcsStorage")).get("bucketName").toString())
                                .build();

                        GCSResource.Builder builder = GCSResource.newBuilder()
                                .setGcsStorage(storage)
                                .setResourceId(r.get("resourceId").toString());

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();

                    }).collect(Collectors.toList());
            return gcsResources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }
    }

    @Override
    public GCSResource createGCSResource(GCSResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateGCSResource(GCSResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteGCSResource(GCSResourceDeleteRequest request) throws Exception {
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

                        DropboxResource.Builder builder = DropboxResource.newBuilder()
                                .setResourceId(r.get("resourceId").toString())
                                .setDropboxStorage(storage);

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();

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
    public Optional<DropboxResource> getDropboxResource(DropboxResourceGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<DropboxResource> dropboxResources = (List<DropboxResource>) resourceList.stream()
                    .filter(resource -> "DROPBOX".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;
                        String resourcePath = r.get("resourcePath").toString();
                        resourcePath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;

                        DropboxStorage storage = DropboxStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("dropboxStorage")).get("storageId").toString())
                                .build();

                        DropboxResource.Builder builder = DropboxResource.newBuilder()
                                .setResourceId(r.get("resourceId").toString())
                                .setDropboxStorage(storage);

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();

                    }).collect(Collectors.toList());
            return dropboxResources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }
    }

    @Override
    public DropboxResource createDropboxResource(DropboxResourceCreateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateDropboxResource(DropboxResourceUpdateRequest request) throws Exception {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteDropboxResource(DropboxResourceDeleteRequest request) throws Exception {
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

    @Override
    public Optional<FTPResource> getFTPResource(FTPResourceGetRequest request) throws Exception {
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream(resourceFile);

        JSONParser jsonParser = new JSONParser();

        if (inputStream == null) {
            throw new IOException("resources file not found");
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {

            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            List<FTPResource> ftpResources = (List<FTPResource>) resourceList.stream()
                    .filter(resource -> "FTP".equals(((JSONObject) resource).get("type").toString()))
                    .map(resource -> {
                        JSONObject r = (JSONObject) resource;

                        FTPStorage storage = FTPStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("ftpStorage")).get("storageId").toString())
                                .setHost(((JSONObject)r.get("ftpStorage")).get("host").toString())
                                .setPort(Integer.parseInt(((JSONObject)r.get("ftpStorage")).get("port").toString())).build();

                        FTPResource.Builder builder = FTPResource.newBuilder()
                                .setResourceId(r.get("resourceId").toString())
                                .setFtpStorage(storage);

                        switch (r.get("resourceMode").toString()) {
                            case "FILE":
                                FileResource fileResource = FileResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setFile(fileResource);
                                break;
                            case "DIRECTORY":
                                DirectoryResource directoryResource = DirectoryResource.newBuilder().setResourcePath(r.get("resourcePath").toString()).build();
                                builder = builder.setDirectory(directoryResource);
                                break;
                        }
                        return builder.build();

                    }).collect(Collectors.toList());

            return ftpResources.stream().filter(r -> request.getResourceId().equals(r.getResourceId())).findFirst();
        }
    }

    @Override
    public FTPResource createFTPResource(FTPResourceCreateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean updateFTPResource(FTPResourceUpdateRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }

    @Override
    public boolean deleteFTPResource(FTPResourceDeleteRequest request) {
        throw new UnsupportedOperationException("Operation is not supported in backend");
    }
}
