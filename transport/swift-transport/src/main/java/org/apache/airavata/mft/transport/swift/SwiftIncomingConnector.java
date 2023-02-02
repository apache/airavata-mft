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

package org.apache.airavata.mft.transport.swift;

import org.apache.airavata.mft.core.api.ConnectorConfig;
import org.apache.airavata.mft.core.api.IncomingChunkedConnector;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecretGetRequest;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageGetRequest;
import org.jclouds.ContextBuilder;
import org.jclouds.http.options.GetOptions;
import org.jclouds.openstack.keystone.auth.config.CredentialTypes;
import org.jclouds.openstack.keystone.config.KeystoneProperties;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class SwiftIncomingConnector implements IncomingChunkedConnector {

    private static final Logger logger = LoggerFactory.getLogger(SwiftIncomingConnector.class);

    private SwiftApi swiftApi;
    private ObjectApi objectApi;
    private String resourcePath;

    @Override
    public void init(ConnectorConfig cc) throws Exception {

        SwiftStorage swiftStorage = cc.getStorage().getSwift();
        this.resourcePath = cc.getResourcePath();
        SwiftSecret swiftSecret = cc.getSecret().getSwift();
        swiftApi = SwiftUtil.createSwiftApi(swiftSecret, swiftStorage);
        objectApi = swiftApi.getObjectApi(swiftStorage.getRegion(), swiftStorage.getContainer());
    }

    @Override
    public void complete() throws Exception {
        if (swiftApi != null) {
            swiftApi.close();
        }
    }

    @Override
    public void failed() throws Exception {

    }

    @Override
    public void downloadChunk(int chunkId, long startByte, long endByte, String downloadFile) throws Exception {
        SwiftObject swiftObject = objectApi.get(
                resourcePath,
                GetOptions.Builder.range(startByte, endByte));

        InputStream inputStream = swiftObject.getPayload().openStream();

        File targetFile = new File(downloadFile);

        java.nio.file.Files.copy(
                inputStream,
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        inputStream.close();
    }

    @Override
    public InputStream downloadChunk(int chunkId, long startByte, long endByte) throws Exception {

        SwiftObject swiftObject = objectApi.get(
                resourcePath,
                GetOptions.Builder.range(startByte, endByte));

        return swiftObject.getPayload().openStream();
    }
}
