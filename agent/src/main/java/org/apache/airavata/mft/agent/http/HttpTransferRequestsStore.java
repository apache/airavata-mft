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

package org.apache.airavata.mft.agent.http;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpTransferRequestsStore {

    final private Map<String, HttpTransferRequest> downloadRequestStore = new HashMap<>();
    final private Map<String, HttpTransferRequest> uploadRequestStore = new HashMap<>();

    public String addDownloadRequest(HttpTransferRequest request) {
        String randomUrl = UUID.randomUUID().toString();
        downloadRequestStore.put(randomUrl, request);
        return randomUrl;
    }

    public HttpTransferRequest getDownloadRequest(String url) {

        //TODO  Need to block concurrent calls to same url as connectors are not thread safe
        HttpTransferRequest request = downloadRequestStore.get(url);
        return request;
    }

    public String addUploadRequest(HttpTransferRequest request) {
        String randomUrl = UUID.randomUUID().toString();
        uploadRequestStore.put(randomUrl, request);
        return randomUrl;
    }

    public HttpTransferRequest getUploadRequest(String url) {

        //TODO  Need to block concurrent calls to same url as connectors are not thread safe
        HttpTransferRequest request = downloadRequestStore.get(url);
        return request;
    }
}
