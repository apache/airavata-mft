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

package org.apache.airavata.mft.command.line.sub.gcs;

import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecretCreateRequest;
import org.apache.airavata.mft.resource.service.gcs.GCSStorageServiceGrpc;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageCreateRequest;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecret;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecretCreateRequest;
import org.apache.airavata.mft.storage.stubs.storagesecret.StorageSecretServiceGrpc;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command( name = "add" )
public class GCSAddSubCommand implements Callable<Integer>
{

    @CommandLine.Option( names = {"-n", "--name"}, description = "Storage Name" )
    private String name;

    @CommandLine.Option( names = {"-b", "--bucket"}, description = "Bucket Name" )
    private String bucket;

    @CommandLine.Option( names = {"-s", "--storageId"}, description = "Storage ID" )
    private String storageId;

    @CommandLine.Option( names = {"-pid", "--projectId"}, description = "Project Id", required = true )
    private String projectId;

    @CommandLine.Option( names = {"-p", "--privateKey"}, description = "Private Key", required = true )
    private String privateKey;

    @CommandLine.Option( names = {"-c", "--clientEmail"}, description = "Client Email", required = true )
    private String clientEmail;



    @Override
    public Integer call() throws Exception
    {
        AuthToken authToken = AuthToken.newBuilder().build();

        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        GCSSecret gcsSecret = mftApiClient.getSecretServiceClient().gcs().
                createGCSSecret( GCSSecretCreateRequest.newBuilder().
                        setProjectId( projectId ).
                        setPrivateKey( privateKey ).
                        setClientEmail( clientEmail ).
                        setAuthzToken( authToken ).build() );

        System.out.println( "Created the gcs secret " + gcsSecret.getSecretId() );

        GCSStorageServiceGrpc.GCSStorageServiceBlockingStub gcsStorageClient =
                mftApiClient.getStorageServiceClient().gcs();

        GCSStorage gcsStorage = gcsStorageClient.createGCSStorage( GCSStorageCreateRequest.newBuilder()
                .setStorageId( storageId )
                .setBucketName( bucket )
                .setName( name ).build() );
        System.out.println( "Created gcs storage " + gcsStorage.getStorageId() );

        StorageSecretServiceGrpc.StorageSecretServiceBlockingStub storageSecretClient =
                mftApiClient.getStorageServiceClient().storageSecret();
        StorageSecret storageSecret = storageSecretClient.createStorageSecret( StorageSecretCreateRequest.newBuilder()
                .setStorageId( gcsStorage.getStorageId() )
                .setSecretId( gcsSecret.getSecretId() )
                .setType( StorageSecret.StorageType.GCS ).build() );

        System.out.println( "Storage Id " + gcsStorage.getStorageId() );

        return 0;
    }
}
