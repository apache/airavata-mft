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

package org.apache.airavata.mft.transport.gcp;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;

import org.apache.airavata.mft.agent.stub.*;
import org.apache.airavata.mft.core.api.MetadataCollector;
import org.apache.airavata.mft.credential.stubs.gcs.GCSSecret;
import org.apache.airavata.mft.resource.stubs.gcs.storage.GCSStorage;

import java.io.File;
import java.security.PrivateKey;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GCSMetadataCollector implements MetadataCollector
{

    boolean initialized = false;

    private GCSStorage gcsStorage;
    private GCSSecret gcsSecret;

    @Override
    public void init( StorageWrapper storage, SecretWrapper secret )
    {
        this.gcsStorage = storage.getGcs();
        this.gcsSecret = secret.getGcs();
        this.initialized = true;
    }

    private void checkInitialized()
    {
        if ( !initialized )
        {
            throw new IllegalStateException( "GCS Metadata Collector is not initialized" );
        }
    }

    @Override
    public ResourceMetadata getResourceMetadata( String resourcePath, boolean recursiveSearch ) throws Exception
    {

        checkInitialized();

        PrivateKey privKey = GCSUtil.getPrivateKey( gcsSecret.getPrivateKey() );

        try ( Storage storage = StorageOptions.newBuilder().setCredentials(
                ServiceAccountCredentials.newBuilder()
                        .setProjectId( gcsSecret.getProjectId() )
                        .setPrivateKey( privKey )
                        .setClientEmail( gcsSecret.getClientEmail() )
                        .build() ).build().getService() )
        {

            ResourceMetadata.Builder resourceBuilder = ResourceMetadata.newBuilder();

            if ( gcsStorage.getBucketName().isEmpty() && resourcePath.isEmpty() ) // Load bucket list
            {
                Page<Bucket> buckets = storage.list();
                DirectoryMetadata.Builder parentDir = DirectoryMetadata.newBuilder();
                parentDir.setResourcePath( "" );
                parentDir.setFriendlyName( "" );

                for ( Bucket b : buckets.getValues() )
                {
                    DirectoryMetadata.Builder bucketDir = DirectoryMetadata.newBuilder();
                    bucketDir.setFriendlyName( b.getName() );
                    bucketDir.setResourcePath( b.getName() );
                    bucketDir.setCreatedTime( b.getCreateTimeOffsetDateTime().getLong( ChronoField.INSTANT_SECONDS ) );
                    bucketDir.setUpdateTime( b.getUpdateTimeOffsetDateTime().getLong( ChronoField.INSTANT_SECONDS ) );
                    parentDir.addDirectories( bucketDir );
                }

                resourceBuilder.setDirectory( parentDir );
                return resourceBuilder.build();
            }

            // If directory path or top level bucket path
            final String dirPath = resourcePath.endsWith( "/" ) ? resourcePath : resourcePath + "/";
            if ( resourcePath.endsWith( "/" ) || resourcePath.isEmpty() )
            { // Directory

                Page<Blob> blob =
                        storage.list( gcsStorage.getBucketName(), Storage.BlobListOption.prefix( resourcePath ),
                                Storage.BlobListOption.currentDirectory() );
                resourceBuilder.setDirectory( processDirectory( resourcePath, blob ) );
            }
            else
            {
                try
                {
                    Blob blob = storage.get( gcsStorage.getBucketName(), resourcePath,
                            Storage.BlobGetOption.fields( Storage.BlobField.values() ) );

                    if ( blob != null )
                    {
                        FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
                        fileBuilder.setFriendlyName( blob.getName() );
                        fileBuilder.setResourcePath( resourcePath );
                        fileBuilder.setCreatedTime( blob.getCreateTimeOffsetDateTime().getLong( ChronoField.INSTANT_SECONDS ) );
                        fileBuilder.setUpdateTime( blob.getUpdateTimeOffsetDateTime().getLong( ChronoField.INSTANT_SECONDS ) );
                        fileBuilder.setResourceSize( blob.getSize() );
                        fileBuilder.setMd5Sum( blob.getMd5() );
                        resourceBuilder.setFile( fileBuilder );
                    }
                    else
                    {
                        Page<Blob> blobs =
                                storage.list( gcsStorage.getBucketName(), Storage.BlobListOption.prefix( dirPath ),
                                        Storage.BlobListOption.currentDirectory() );
                        resourceBuilder.setDirectory( processDirectory( resourcePath + "/", blobs ) );
                    }
                }
                catch ( Exception e )
                {
                    resourceBuilder.setError( MetadataFetchError.NOT_FOUND );
                }
            }
            return resourceBuilder.build();
        }
    }

    @Override
    public Boolean isAvailable( String resourcePath )
            throws Exception
    {
        checkInitialized();
        PrivateKey privKey = GCSUtil.getPrivateKey( gcsSecret.getPrivateKey() );

        try ( Storage storage = StorageOptions.newBuilder().setCredentials(
                ServiceAccountCredentials.newBuilder().setProjectId( gcsSecret.getProjectId() ).setPrivateKey( privKey )
                        .setClientEmail( gcsSecret.getClientEmail() ).build() ).build().getService() )
        {
            Blob blob = storage.get( gcsStorage.getBucketName(), resourcePath,
                    Storage.BlobGetOption.fields( Storage.BlobField.values() ) );
            if ( blob != null )
            {
                return true;
            }
            else
            {
                final String dirPath = resourcePath.endsWith( "/" ) ? resourcePath : resourcePath + "/";
                try
                {
                    Page<Blob> blobs =
                            storage.list( gcsStorage.getBucketName(), Storage.BlobListOption.currentDirectory(),
                                    Storage.BlobListOption.prefix( dirPath ) );
                    return true;
                }
                catch ( Exception e )
                {
                    return false;
                }
            }
        }
    }

    private DirectoryMetadata.Builder processDirectory( String resourcePath, Page<Blob> objectListing )
    {
        Iterable<Blob> objectSummaries = objectListing.getValues();
        Map<String, DirectoryMetadata.Builder> subDirCache = new HashMap<>();
        Map<String, List<String>> childTree = new HashMap<>();
        childTree.put( resourcePath, new ArrayList<>() );
        DirectoryMetadata.Builder dirBuilder = DirectoryMetadata.newBuilder();
        subDirCache.put( resourcePath, dirBuilder );

        for ( Blob summary : objectSummaries )
        {
            buildStructureRecursively( resourcePath, summary.getName(), summary, subDirCache, childTree );
        }

        registerChildren( resourcePath, subDirCache, childTree );
        return dirBuilder;
    }

    private void registerChildren( String parentPath, Map<String, DirectoryMetadata.Builder> directoryStore,
                                   Map<String, List<String>> childTree )
    {
        for ( String childDir : childTree.get( parentPath ) )
        {
            registerChildren( childDir, directoryStore, childTree );
            directoryStore.get( parentPath ).addDirectories( directoryStore.get( childDir ) );
        }
    }

    private void buildStructureRecursively( String basePath, String filePath, Blob summary,
                                            Map<String, DirectoryMetadata.Builder> directoryStore,
                                            Map<String, List<String>> childTree )
    {
        String relativePath = filePath.substring( basePath.length() );
        if ( relativePath.contains( "/" ) )
        { // A Directory
            String[] pathSections = relativePath.split( "/" );
            String thisDirKey = basePath + pathSections[0] + "/";
            if ( !directoryStore.containsKey( thisDirKey ) )
            {
                DirectoryMetadata.Builder subDirBuilder = DirectoryMetadata.newBuilder();
                subDirBuilder.setResourcePath( thisDirKey );
                subDirBuilder.setFriendlyName( pathSections[0] );
                directoryStore.put( thisDirKey, subDirBuilder );
                childTree.get( basePath ).add( thisDirKey );
                childTree.put( thisDirKey, new ArrayList<>() );
            }
        }
        else if ( !basePath.equals( summary.getName() ) )
        { // A File
            FileMetadata.Builder fileBuilder = FileMetadata.newBuilder();
            fileBuilder.setUpdateTime( summary.getUpdateTimeOffsetDateTime().getLong( ChronoField.INSTANT_SECONDS ) );
            fileBuilder.setCreatedTime( summary.getCreateTimeOffsetDateTime().getLong( ChronoField.INSTANT_SECONDS ) );
            fileBuilder.setResourcePath( summary.getName() );
            fileBuilder.setFriendlyName( new File( summary.getName() ).getName() );
            fileBuilder.setResourceSize( summary.getSize() );
            directoryStore.get( basePath ).addFiles( fileBuilder );
        }
    }
}
