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
import org.apache.airavata.mft.resource.service.azure.AzureResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.box.BoxResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.dropbox.DropboxServiceGrpc;
import org.apache.airavata.mft.resource.service.ftp.FTPResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.gcs.GCSResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.local.LocalResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.s3.S3ResourceServiceGrpc;
import org.apache.airavata.mft.resource.service.scp.SCPResourceServiceGrpc;

import java.io.Closeable;
import java.io.IOException;

public class ResourceServiceClient implements Closeable {
    private ManagedChannel channel;

    ResourceServiceClient(ManagedChannel channel) {
        this.channel = channel;
    }

    public SCPResourceServiceGrpc.SCPResourceServiceBlockingStub scp() {
        return SCPResourceServiceGrpc.newBlockingStub(channel);
    }

    public LocalResourceServiceGrpc.LocalResourceServiceBlockingStub local() {
        return LocalResourceServiceGrpc.newBlockingStub(channel);
    }

    public S3ResourceServiceGrpc.S3ResourceServiceBlockingStub s3() {
        return S3ResourceServiceGrpc.newBlockingStub(channel);
    }

    public FTPResourceServiceGrpc.FTPResourceServiceBlockingStub ftp() {
        return FTPResourceServiceGrpc.newBlockingStub(channel);
    }

    public AzureResourceServiceGrpc.AzureResourceServiceBlockingStub azure() {
        return AzureResourceServiceGrpc.newBlockingStub(channel);
    }

    public GCSResourceServiceGrpc.GCSResourceServiceBlockingStub gcs() {
        return GCSResourceServiceGrpc.newBlockingStub(channel);
    }

    public BoxResourceServiceGrpc.BoxResourceServiceBlockingStub box() {
        return BoxResourceServiceGrpc.newBlockingStub(channel);
    }

    public DropboxServiceGrpc.DropboxServiceBlockingStub dropbox() {
        return DropboxServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void close() throws IOException {
        this.channel.shutdown();
    }
}
