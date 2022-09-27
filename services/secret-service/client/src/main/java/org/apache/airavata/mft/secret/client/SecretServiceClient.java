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

package org.apache.airavata.mft.secret.client;

import io.grpc.ManagedChannel;
import org.apache.airavata.mft.credential.service.azure.AzureSecretServiceGrpc;
import org.apache.airavata.mft.credential.service.box.BoxSecretServiceGrpc;
import org.apache.airavata.mft.credential.service.dropbox.DropboxSecretServiceGrpc;
import org.apache.airavata.mft.credential.service.ftp.FTPSecretServiceGrpc;
import org.apache.airavata.mft.credential.service.gcs.GCSSecretServiceGrpc;
import org.apache.airavata.mft.credential.service.odata.ODataSecretServiceGrpc;
import org.apache.airavata.mft.credential.service.s3.S3SecretServiceGrpc;
import org.apache.airavata.mft.credential.service.scp.SCPSecretServiceGrpc;
import org.apache.airavata.mft.credential.service.swift.SwiftSecretServiceGrpc;

import java.io.Closeable;
import java.io.IOException;

public class SecretServiceClient implements Closeable {

    private ManagedChannel channel;

    SecretServiceClient(ManagedChannel channel) {
        this.channel = channel;
    }

    public SCPSecretServiceGrpc.SCPSecretServiceBlockingStub scp() {
        return SCPSecretServiceGrpc.newBlockingStub(channel);
    }

    public S3SecretServiceGrpc.S3SecretServiceBlockingStub s3() {
        return S3SecretServiceGrpc.newBlockingStub(channel);
    }

    public FTPSecretServiceGrpc.FTPSecretServiceBlockingStub ftp() {
        return FTPSecretServiceGrpc.newBlockingStub(channel);
    }

    public AzureSecretServiceGrpc.AzureSecretServiceBlockingStub azure() {
        return AzureSecretServiceGrpc.newBlockingStub(channel);
    }

    public GCSSecretServiceGrpc.GCSSecretServiceBlockingStub gcs() {
        return GCSSecretServiceGrpc.newBlockingStub(channel);
    }

    public BoxSecretServiceGrpc.BoxSecretServiceBlockingStub box() {
        return BoxSecretServiceGrpc.newBlockingStub(channel);
    }

    public DropboxSecretServiceGrpc.DropboxSecretServiceBlockingStub dropbox() {
        return DropboxSecretServiceGrpc.newBlockingStub(channel);
    }

    public SwiftSecretServiceGrpc.SwiftSecretServiceBlockingStub swift() {
        return SwiftSecretServiceGrpc.newBlockingStub(channel);
    }

    public ODataSecretServiceGrpc.ODataSecretServiceBlockingStub odata() {
        return ODataSecretServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void close() throws IOException {
        this.channel.shutdown();
    }
}
