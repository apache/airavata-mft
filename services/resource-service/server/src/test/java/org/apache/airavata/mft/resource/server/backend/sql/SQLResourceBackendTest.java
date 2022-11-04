package org.apache.airavata.mft.resource.server.backend.sql;

import org.apache.airavata.mft.resource.server.backend.sql.entity.ResolveStorageEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.S3StorageEntity;
import org.apache.airavata.mft.resource.server.backend.sql.repository.ResolveStorageRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.S3StorageRepository;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3Storage;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageCreateRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListRequest;
import org.apache.airavata.mft.resource.stubs.s3.storage.S3StorageListResponse;
import org.dozer.DozerBeanMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQLResourceBackendTest {

    @InjectMocks
    private SQLResourceBackend sqlResourceBackend;

    @Mock
    private S3StorageRepository s3StorageRepository;

    @Mock
    private ResolveStorageRepository resolveStorageRepository;

    @Test
    void successfullyListS3Storage() throws Exception{
        // given
        S3StorageListRequest testRequest = S3StorageListRequest.newBuilder().setOffset(0).setLimit(10).build();
        // mock s3StorageRepository
        List<S3StorageEntity> testResponse = new ArrayList<>();
        S3StorageEntity s3StorageResp = new S3StorageEntity();
        s3StorageResp.setStorageId("test-storage-id-1234");
        s3StorageResp.setName("test-storage-1");
        s3StorageResp.setBucketName("test-bucket-1");
        s3StorageResp.setRegion("us-east-2");
        s3StorageResp.setEndpoint("https://s3.us-east-2.amazonaws.com");
        s3StorageResp.setUseTLS(false);
        testResponse.add(s3StorageResp);
        when(s3StorageRepository.findAll(
                PageRequest.of(testRequest.getOffset(), testRequest.getLimit())))
                .thenReturn(testResponse);

        // when
        S3StorageListResponse response = sqlResourceBackend.listS3Storage(testRequest);

        // then
        verify(s3StorageRepository, times(1)).findAll(
                PageRequest.of(testRequest.getOffset(), testRequest.getLimit()));
        assertEquals(response.getStoragesList().size(), testResponse.size());
        assertFalse(response.getStoragesList().isEmpty());
        assertEquals(response.getStoragesList().get(0).getStorageId(), testResponse.get(0).getStorageId());
    }

    @Test
    void successfullyGetS3Storage() {

    }

    @Test
    void successfullyCreateS3Storage() throws Exception{
        // given
        S3StorageCreateRequest testRequest = S3StorageCreateRequest.newBuilder()
                .setBucketName("test-bucket-1")
                .setRegion("us-east-2")
                //.setStorageId("test-storage-id-1234")
                .setEndpoint("https://s3.us-east-2.amazonaws.com")
                .setUseTLS(false)
                .setName("test-storage-1").build();
        // mock s3StorageRepository save
        S3StorageEntity testResponse = new S3StorageEntity();
        testResponse.setStorageId("test-storage-id-1234");
        testResponse.setName("test-storage-1");
        testResponse.setBucketName("test-bucket-1");
        testResponse.setRegion("us-east-2");
        testResponse.setEndpoint("https://s3.us-east-2.amazonaws.com");
        testResponse.setUseTLS(false);
        DozerBeanMapper mapper = new DozerBeanMapper();
        when(s3StorageRepository.save(any(S3StorageEntity.class))).thenReturn(testResponse);

        // when
        sqlResourceBackend.createS3Storage(testRequest);

        // then
        verify(s3StorageRepository, times(1)).save(any(S3StorageEntity.class));
        verify(resolveStorageRepository, times(1)).save(any(ResolveStorageEntity.class));

    }

    @Test
    void updateS3Storage() {
    }

    @Test
    void deleteS3Storage() {
    }
}