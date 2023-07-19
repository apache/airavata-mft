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

package org.apache.airavata.mft.resource.client;

import io.grpc.ManagedChannel;
import org.apache.airavata.mft.resource.service.azure.AzureStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.box.BoxStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.dropbox.DropboxStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.ftp.FTPStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.gcs.GCSStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.http.HTTPStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.local.LocalStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.odata.ODataStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.s3.S3StorageServiceGrpc;
import org.apache.airavata.mft.resource.service.scp.SCPStorageServiceGrpc;
import org.apache.airavata.mft.resource.service.swift.SwiftStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.storage.common.StorageCommonServiceGrpc;

import java.io.Closeable;
import java.io.IOException;

public class StorageServiceClient implements Closeable {

    private ManagedChannel channel;

    public StorageServiceClient(ManagedChannel channel) {
        this.channel = channel;
    }

    public SCPStorageServiceGrpc.SCPStorageServiceBlockingStub scp() {
        return SCPStorageServiceGrpc.newBlockingStub(channel);
    }

    public LocalStorageServiceGrpc.LocalStorageServiceBlockingStub local() {
        return LocalStorageServiceGrpc.newBlockingStub(channel);
    }

    public S3StorageServiceGrpc.S3StorageServiceBlockingStub s3() {
        return S3StorageServiceGrpc.newBlockingStub(channel);
    }

    public FTPStorageServiceGrpc.FTPStorageServiceBlockingStub ftp() {
        return FTPStorageServiceGrpc.newBlockingStub(channel);
    }

    public AzureStorageServiceGrpc.AzureStorageServiceBlockingStub azure() {
        return AzureStorageServiceGrpc.newBlockingStub(channel);
    }

    public GCSStorageServiceGrpc.GCSStorageServiceBlockingStub gcs() {
        return GCSStorageServiceGrpc.newBlockingStub(channel);
    }

    public BoxStorageServiceGrpc.BoxStorageServiceBlockingStub box() {
        return BoxStorageServiceGrpc.newBlockingStub(channel);
    }

    public DropboxStorageServiceGrpc.DropboxStorageServiceBlockingStub dropbox() {
        return DropboxStorageServiceGrpc.newBlockingStub(channel);
    }

    public SwiftStorageServiceGrpc.SwiftStorageServiceBlockingStub swift() {
        return SwiftStorageServiceGrpc.newBlockingStub(channel);
    }

    public ODataStorageServiceGrpc.ODataStorageServiceBlockingStub odata() {
        return ODataStorageServiceGrpc.newBlockingStub(channel);
    }

    public HTTPStorageServiceGrpc.HTTPStorageServiceBlockingStub http() {
        return HTTPStorageServiceGrpc.newBlockingStub(channel);
    }

    public StorageCommonServiceGrpc.StorageCommonServiceBlockingStub common() {
        return StorageCommonServiceGrpc.newBlockingStub(channel);
    }
    @Override
    public void close() throws IOException {

    }
}
