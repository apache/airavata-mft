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
import org.apache.airavata.mft.resource.service.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileBasedResourceBackend implements ResourceBackend {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedResourceBackend.class);

    @org.springframework.beans.factory.annotation.Value("${file.backend.resource.file}")
    private String resourceFile;

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
        throw new UnsupportedOperationException("Operation is not supported in backend");
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

                        SCPStorage storage = SCPStorage.newBuilder()
                                .setStorageId(((JSONObject)r.get("scpStorage")).get("storageId").toString())
                                .setHost(((JSONObject)r.get("scpStorage")).get("host").toString())
                                .setUser(((JSONObject)r.get("scpStorage")).get("user").toString())
                                .setPort(Integer.parseInt(((JSONObject)r.get("scpStorage")).get("port").toString())).build();

                        SCPResource scpResource = SCPResource.newBuilder()
                                .setResourcePath(r.get("resourcePath").toString())
                                .setResourceId(r.get("resourceId").toString())
                                .setScpStorage(storage).build();

                        return scpResource;
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

                        LocalResource localResource = LocalResource.newBuilder()
                                .setResourcePath(r.get("resourcePath").toString())
                                .setResourceId(r.get("resourceId").toString()).build();

                        return localResource;
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

                        S3Resource s3Resource = S3Resource.newBuilder()
                                .setResourcePath(r.get("resourcePath").toString())
                                .setResourceId(r.get("resourceId").toString())
                                .setBucketName(r.get("bucketName").toString())
                                .setRegion(r.get("region").toString())
                                .build();

                        return s3Resource;
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

                        BoxResource boxResource = BoxResource.newBuilder()
                                .setResourceId(r.get("resourceId").toString())
                                .setBoxFileId(r.get("boxFileId").toString())
                                .build();

                        return boxResource;
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

                        AzureResource azureResource = AzureResource.newBuilder()
                                .setBlobName(r.get("blobName").toString())
                                .setContainer(r.get("container").toString())
                                .setResourceId(r.get("resourceId").toString())
                                .build();

                        return azureResource;
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

                        GCSResource gcsResource = GCSResource.newBuilder()
                                .setBucketName(r.get("bucketName").toString())
                                .setResourceId(r.get("resourceId").toString())
                                .setResourcePath(r.get("resourcePath").toString())
                                .build();

                        return gcsResource;
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
                        DropboxResource dropboxResource = DropboxResource.newBuilder()
                                .setResourceId(r.get("resourceId").toString())
                                .setResourcePath(resourcePath)
                                .build();

                        return dropboxResource;
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
}
