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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileBasedResourceBackend implements ResourceBackend {


    @Override
    public Optional<SCPStorage> getSCPStorage(SCPStorageGetRequest request) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public SCPStorage createSCPStorage(SCPStorageCreateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateSCPStorage(SCPStorageUpdateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteSCPStorage(SCPStorageDeleteRequest request) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateSCPResource(SCPResourceUpdateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteSCPResource(SCPResourceDeleteRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<LocalResource> getLocalResource(LocalResourceGetRequest request) throws Exception {
        JSONParser jsonParser = new JSONParser();
        InputStream inputStream = FileBasedResourceBackend.class.getClassLoader().getResourceAsStream("resources.json");

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Object obj = jsonParser.parse(reader);

            JSONArray resourceList = (JSONArray) obj;

            System.out.println("All resources ");
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateLocalResource(LocalResourceUpdateRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteLocalResource(LocalResourceDeleteRequest request) {
        throw new UnsupportedOperationException();
    }
}
