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
import org.apache.airavata.mft.command.line.CommandLineUtil;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageDeleteRequest;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageDeleteResponse;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageGetRequest;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageListRequest;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorageListResponse;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command( name = "remote", subcommands = {GCSAddSubCommand.class} )
public class GCSRemoteSubCommand
{

    @CommandLine.Command( name = "get" )
    void getGSCResource( @CommandLine.Parameters( index = "0" ) String resourceId )
    {
        System.out.println( "Getting GCS Resource " + resourceId );
        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        GCSStorage gcsStorage = mftApiClient.getStorageServiceClient().gcs()
                .getGCSStorage( GCSStorageGetRequest.newBuilder().setStorageId( resourceId ).build() );

        List<GCSStorage> storagesList = new ArrayList<>();
        storagesList.add( gcsStorage );

        int[] columnWidth = {40, 15, 15};
        String[][] content = new String[storagesList.size() + 1][3];
        String[] headers = {"STORAGE ID", "NAME", "BUCKET"};
        content[0] = headers;

        for ( int i = 1; i <= storagesList.size(); i++ )
        {
            GCSStorage gcs= storagesList.get( i - 1 );
            content[i][0] = gcs.getStorageId();
            content[i][1] = gcs.getName();
            content[i][2] = gcs.getBucketName();
        }
        CommandLineUtil.printTable( columnWidth, content );
    }

    @CommandLine.Command( name = "delete" )
    void deleteGCSResource( @CommandLine.Parameters( index = "0" ) String resourceId )
    {
        System.out.println( "Deleting GCS Resource " + resourceId );
        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        GCSStorageDeleteResponse gcsStorageDeleteResponse = mftApiClient.getStorageServiceClient().gcs()
                .deleteGCSStorage( GCSStorageDeleteRequest.newBuilder().setStorageId( resourceId ).build() );
        System.out.println( "Delete GCS Resource status : " + gcsStorageDeleteResponse.getStatus() );

    }

    @CommandLine.Command( name = "list" )
    void listS3Resource()
    {
        System.out.println( "Listing GSC Resource" );
        MFTApiClient mftApiClient = MFTApiClient.MFTApiClientBuilder.newBuilder().build();

        GCSStorageListResponse gcsStorageListResponse = mftApiClient.getStorageServiceClient().gcs()
                .listGCSStorage( GCSStorageListRequest.newBuilder().setOffset( 0 ).setLimit( 10 ).build() );

        List<GCSStorage> storagesList = gcsStorageListResponse.getStoragesList();

        int[] columnWidth = {40, 15, 15};
        String[][] content = new String[storagesList.size() + 1][3];
        String[] headers = {"STORAGE ID", "NAME", "BUCKET"};
        content[0] = headers;


        for ( int i = 1; i <= storagesList.size(); i++ )
        {
            GCSStorage gcsStorage = storagesList.get( i - 1 );
            content[i][0] = gcsStorage.getStorageId();
            content[i][1] = gcsStorage.getName();
            content[i][2] = gcsStorage.getBucketName();

        }

        CommandLineUtil.printTable( columnWidth, content );
    }


}
