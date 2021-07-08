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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpTransferRequestsStore {

    private static final Logger logger = LoggerFactory.getLogger(HttpTransferRequestsStore.class);

    final private Map<String, HttpTransferRequest> downloadRequestStore = new ConcurrentHashMap<>();
    final private Map<String, HttpTransferRequest> uploadRequestStore = new ConcurrentHashMap<>();

    final private ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
    private long entryExpiryTimeMS = 300 * 1000;

    public HttpTransferRequestsStore() {
        monitor.scheduleWithFixedDelay(()-> {
            logger.debug("Cleaning up the request store..");
            downloadRequestStore.keySet().forEach(key -> {
                if ((System.currentTimeMillis() - downloadRequestStore.get(key).getCreatedTime()) > entryExpiryTimeMS) {
                    downloadRequestStore.remove(key);
                    logger.info("Removed url {} from download cache", key);
                }
            });

            uploadRequestStore.keySet().forEach(key -> {
                if ((System.currentTimeMillis() - uploadRequestStore.get(key).getCreatedTime()) > entryExpiryTimeMS) {
                    uploadRequestStore.remove(key);
                    logger.info("Removed url {} from upload cache", key);
                }
            });
        }, 2, 10, TimeUnit.SECONDS);
    }

    public String addDownloadRequest(HttpTransferRequest request) {
        String randomUrl = UUID.randomUUID().toString();
        downloadRequestStore.put(randomUrl, request);
        return randomUrl;
    }

    public HttpTransferRequest getDownloadRequest(String url) {

        //TODO  Need to block concurrent calls to same url as connectors are not thread safe
        HttpTransferRequest request = downloadRequestStore.get(url);
        if (request != null) {
            downloadRequestStore.remove(url);
        }
        return request;
    }

    public String addUploadRequest(HttpTransferRequest request) {
        String randomUrl = UUID.randomUUID().toString();
        uploadRequestStore.put(randomUrl, request);
        return randomUrl;
    }

    public HttpTransferRequest getUploadRequest(String url) {

        //TODO  Need to block concurrent calls to same url as connectors are not thread safe
        HttpTransferRequest request = uploadRequestStore.get(url);
        if (request != null) {
            uploadRequestStore.remove(url);
        }
        return request;
    }
}
