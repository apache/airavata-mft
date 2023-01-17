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

package org.apache.airavata.mft.resource.server.handler;


import io.grpc.internal.testing.StreamRecorder;
import org.apache.airavata.mft.resource.server.backend.ResourceBackend;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
class S3ServiceHandlerTest
{

    @InjectMocks
    private S3ServiceHandler s3ServiceHandler;

    @Mock
    private ResourceBackend backend;


    @BeforeEach
    void setUp()
    {
        s3ServiceHandler = new S3ServiceHandler();
        MockitoAnnotations.openMocks( this );
    }

    @Test
    void listS3Storage() throws Exception
    {

        S3StorageListRequest request1 = S3StorageListRequest.newBuilder().setOffset( 0 ).setLimit( 10 ).build();
        when( backend.listS3Storage( request1 ) ).thenReturn( S3StorageListResponse.newBuilder().addStorages( 0,
                S3Storage.newBuilder().setStorageId( "1" ).setBucketName( "testBkt" ).setName( "testJCV" )
                        .setRegion( "usa-east" ) ).build() );

        S3StorageListRequest request = S3StorageListRequest.newBuilder().setOffset( 0 ).setLimit( 10 ).build();
        StreamRecorder<S3StorageListResponse> responseOb = StreamRecorder.create();
        s3ServiceHandler.listS3Storage( request, responseOb );
        if ( !responseOb.awaitCompletion( 5, TimeUnit.SECONDS ) )
        {
            System.out.println( "Time out !" );
            fail();
        }
        List<S3StorageListResponse> results = responseOb.getValues();
        assertEquals( 1, results.size() );
        S3StorageListResponse response = results.get( 0 );
        assertEquals( S3StorageListResponse.newBuilder().addStorages( 0,
                S3Storage.newBuilder().setStorageId( "1" ).setBucketName( "testBkt" ).setName( "testJCV" )
                        .setRegion( "usa-east" ) ).build(), response );
    }

}