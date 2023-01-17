package org.apache.airavata.mft.resource.server.backend.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.airavata.mft.resource.server.backend.sql.entity.S3StorageEntity;
import org.apache.airavata.mft.resource.server.backend.sql.repository.S3StorageRepository;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageGetRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
class SQLResourceBackendTest
{
    @InjectMocks
    private SQLResourceBackend sqlResourceBackend;
    @Mock
    private S3StorageRepository s3StorageRepository;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks( this );
    }

    @Test
    void listS3Storage() throws Exception
    {
        List<S3StorageEntity> storageList = new ArrayList<>();
        S3StorageEntity storage = new S3StorageEntity();
        storage.setStorageId( String.valueOf( 1 ) );
        storage.setBucketName( "testBucket" );
        storage.setName( "Bucket" );
        storage.setRegion( "usa-east" );
        storage.setEndpoint( "aws.com" );
        storage.setUseTLS( true );
        storageList.add( storage );
        when( s3StorageRepository.findAll( PageRequest.of( 1, 5 ) ) ).thenReturn( storageList );
        S3StorageListRequest request = S3StorageListRequest.newBuilder().setLimit( 5 ).setOffset( 1 ).build();
        S3StorageListResponse response = sqlResourceBackend.listS3Storage( request );
        assertTrue( response.getStoragesList().size() > 0 );
    }

    @Test
    void getS3Storage() throws Exception
    {
        S3StorageEntity storage = new S3StorageEntity();
        storage.setStorageId( String.valueOf( 1 ) );
        storage.setBucketName( "testBucket" );
        storage.setName( "Bucket" );
        storage.setRegion( "usa-east" );
        storage.setEndpoint( "aws.com" );
        storage.setUseTLS( true );
        when( s3StorageRepository.findById( "1" ) ).thenReturn( Optional.of( storage ) );
        S3StorageGetRequest request = S3StorageGetRequest.newBuilder().setStorageId( "1" ).build();
        Optional<S3Storage> response = sqlResourceBackend.getS3Storage( request );
        assertTrue( response.isPresent() );
        assertEquals( "1", ( (S3Storage) response.get() ).getStorageId() );
    }

}