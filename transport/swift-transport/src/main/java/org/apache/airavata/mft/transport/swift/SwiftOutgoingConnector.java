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
import org.apache.airavata.mft.core.api.OutgoingChunkedConnector;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage;
import org.jclouds.io.payloads.InputStreamPayload;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.domain.Segment;
import org.jclouds.openstack.swift.v1.features.ObjectApi;
import org.jclouds.openstack.swift.v1.features.StaticLargeObjectApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SwiftOutgoingConnector implements OutgoingChunkedConnector {

    private static final Logger logger = LoggerFactory.getLogger(SwiftOutgoingConnector.class);

    private SwiftApi swiftApi;
    private ObjectApi objectApi;
    private StaticLargeObjectApi staticLargeObjectApi;
    private String resourcePath;

    private final Map<Integer, Segment> segmentMap = new ConcurrentHashMap();

    // Referring to https://www.mirantis.com/blog/large-objects-in-cloud-storages/

    @Override
    public void init(ConnectorConfig cc) throws Exception {

        SwiftStorage swiftStorage = cc.getStorage().getSwift();

        this.resourcePath = cc.getResourcePath();

        SwiftSecret swiftSecret = cc.getSecret().getSwift();
        swiftApi = SwiftUtil.getInstance().leaseSwiftApi(swiftSecret);
        objectApi = swiftApi.getObjectApi(swiftStorage.getRegion(), swiftStorage.getContainer());
        staticLargeObjectApi = swiftApi.getStaticLargeObjectApi(swiftStorage.getRegion(), swiftStorage.getContainer());
    }

    @Override
    public void complete() throws Exception {

        List<Segment> segments = new ArrayList<>();
        for (int id = 0; id < segmentMap.size(); id ++) {
            segments.add(segmentMap.get(id));
        }

        String etag = staticLargeObjectApi.replaceManifest(resourcePath,
                segments, new HashMap<>());

        //if (swiftApi != null) {
        //    swiftApi.close();
        //}
    }

    @Override
    public void failed() throws Exception {

    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, String uploadFile) throws Exception {
        InputStream fis = new FileInputStream(uploadFile);
        uploadChunk(chunkId, startByte, endByte, fis);
    }

    @Override
    public void uploadChunk(int chunkId, long startByte, long endByte, InputStream inputStream) throws Exception {
        String etag = objectApi.put(resourcePath + chunkId, new InputStreamPayload(inputStream));
        Segment segment = Segment.builder().etag(etag)
                .path(resourcePath + chunkId)
                .sizeBytes(endByte - startByte).build();
        segmentMap.put(chunkId, segment);
    }
}
