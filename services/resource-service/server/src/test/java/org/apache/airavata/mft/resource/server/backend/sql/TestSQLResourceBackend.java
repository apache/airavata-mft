package org.apache.airavata.mft.resource.server.backend.sql;

import org.apache.airavata.mft.resource.server.backend.sql.entity.LocalResourceEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.SCPResourceEntity;
import org.apache.airavata.mft.resource.server.backend.sql.entity.SCPStorageEntity;
import org.apache.airavata.mft.resource.server.backend.sql.repository.LocalResourceRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.SCPResourceRepository;
import org.apache.airavata.mft.resource.server.backend.sql.repository.SCPStorageRepository;
import org.apache.airavata.mft.resource.service.*;
import org.dozer.MappingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestSQLResourceBackend {

    @Test
    public void testGetSCPStorage() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPStorageGetRequest scpStorageGetRequest = Mockito.mock(SCPStorageGetRequest.class);
        SCPStorageRepository scpStorageRepository = Mockito.mock(SCPStorageRepository.class);
        SCPStorageEntity scpStorageEntity = new SCPStorageEntity();

        String host = "host";
        int port = 8080;
        String storageId = "randomStorageId";
        int user = 1;

        scpStorageEntity.setHost(host);
        scpStorageEntity.setPort(port);
        scpStorageEntity.setStorageId(storageId);
        scpStorageEntity.setUser(user);

        Mockito.doReturn(scpStorageRepository).when(sqlResourceBackend).getScpStorageRepository();
        Mockito.when(scpStorageRepository.findByStorageId(null)).thenReturn(Optional.of(scpStorageEntity));

        Optional<SCPStorage> scpStorageOptional = sqlResourceBackend.getSCPStorage(scpStorageGetRequest);
        assertTrue(scpStorageOptional.isPresent());
        assertEquals(host, scpStorageOptional.get().getHost());
        assertEquals(port, scpStorageOptional.get().getPort());
        assertEquals(storageId, scpStorageOptional.get().getStorageId());
        assertEquals(String.valueOf(user), scpStorageOptional.get().getUser());
    }

    @Test
    public void testGetSCPStorage_IncompleteSCPStorageEntity() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPStorageGetRequest scpStorageGetRequest = Mockito.mock(SCPStorageGetRequest.class);
        SCPStorageRepository scpStorageRepository = Mockito.mock(SCPStorageRepository.class);
        SCPStorageEntity scpStorageEntity = new SCPStorageEntity();

        String host = "host";
        int port = 8080;

        scpStorageEntity.setHost(host);
        scpStorageEntity.setPort(port);

        Mockito.doReturn(scpStorageRepository).when(sqlResourceBackend).getScpStorageRepository();
        Mockito.when(scpStorageRepository.findByStorageId(null)).thenReturn(Optional.of(scpStorageEntity));

        assertThrows(MappingException.class, () -> sqlResourceBackend.getSCPStorage(scpStorageGetRequest));
    }

    @Test
    public void testCreateSCPStorage() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPStorageCreateRequest scpStorageCreateRequest = Mockito.mock(SCPStorageCreateRequest.class);
        SCPStorageRepository scpStorageRepository = Mockito.mock(SCPStorageRepository.class);

        String host = "host";
        int port = 8080;
        int user = 1;
        String storageId = "storageId";

        Mockito.when(scpStorageCreateRequest.getHost()).thenReturn(host);

        Mockito.when(scpStorageRepository.save(Mockito.any(SCPStorageEntity.class))).thenAnswer(invocation -> {
            SCPStorageEntity entity = invocation.getArgument(0);
            entity.setUser(user);
            entity.setPort(port);
            entity.setStorageId(storageId);
            return entity;
        });
        Mockito.when(sqlResourceBackend.getScpStorageRepository()).thenReturn(scpStorageRepository);

        SCPStorage scpStorage = sqlResourceBackend.createSCPStorage(scpStorageCreateRequest);
        assertEquals(host, scpStorage.getHost());
        assertEquals(port, scpStorage.getPort());
        assertEquals(String.valueOf(user), scpStorage.getUser());
        assertEquals(storageId, scpStorage.getStorageId());
    }

    @Test
    public void testCreateSCPStorage_IncompleteSCPCreateRequest() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPStorageCreateRequest scpStorageCreateRequest = Mockito.mock(SCPStorageCreateRequest.class);
        SCPStorageRepository scpStorageRepository = Mockito.mock(SCPStorageRepository.class);

        String host = "host";
        int port = 8080;
        int user = 1;

        Mockito.when(scpStorageCreateRequest.getHost()).thenReturn(host);

        Mockito.when(scpStorageRepository.save(Mockito.any(SCPStorageEntity.class))).thenAnswer(invocation -> {
            SCPStorageEntity entity = invocation.getArgument(0);
            entity.setUser(user);
            entity.setPort(port);
            return entity;
        });
        Mockito.when(sqlResourceBackend.getScpStorageRepository()).thenReturn(scpStorageRepository);

        assertThrows(MappingException.class, () -> sqlResourceBackend.createSCPStorage(scpStorageCreateRequest));
    }

    @Test
    public void testCreateSCPStorage_EmptySCPCreateRequest() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPStorageCreateRequest scpStorageCreateRequest = Mockito.mock(SCPStorageCreateRequest.class);
        SCPStorageRepository scpStorageRepository = Mockito.mock(SCPStorageRepository.class);

        Mockito.doReturn(scpStorageRepository).when(sqlResourceBackend).getScpStorageRepository();

        assertThrows(MappingException.class, () -> sqlResourceBackend.createSCPStorage(scpStorageCreateRequest));
    }

    @Test
    public void testGetSCPResource() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPResourceGetRequest scpResourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        SCPResourceRepository scpResourceRepository = Mockito.mock(SCPResourceRepository.class);
        SCPResourceEntity scpResourceEntity = Mockito.spy(new SCPResourceEntity());

        SCPStorageEntity scpStorageEntity = new SCPStorageEntity();

        String host = "host";
        int port = 8080;
        String storageId = "randomStorageId";
        int user = 1;

        scpStorageEntity.setHost(host);
        scpStorageEntity.setPort(port);
        scpStorageEntity.setStorageId(storageId);
        scpStorageEntity.setUser(user);

        String resourceId = "resourceId";
        String resourcePath = "resourcePath";
        String scpStorageId = "storageId";

        scpResourceEntity.setResourceId(resourceId);
        scpResourceEntity.setResourcePath(resourcePath);
        scpResourceEntity.setScpStorage(scpStorageEntity);
        scpResourceEntity.setScpStorageId(scpStorageId);

        Mockito.doReturn(scpResourceRepository).when(sqlResourceBackend).getScpResourceRepository();
        Mockito.when(scpResourceRepository.findByResourceId(null)).thenReturn(Optional.of(scpResourceEntity));

        Optional<SCPResource> scpResourceOptional = sqlResourceBackend.getSCPResource(scpResourceGetRequest);
        assertTrue(scpResourceOptional.isPresent());
        assertEquals(resourceId, scpResourceOptional.get().getResourceId());
        assertEquals(resourcePath, scpResourceOptional.get().getResourcePath());
        assertEquals(storageId, scpResourceOptional.get().getScpStorage().getStorageId());
        assertEquals(host, scpResourceOptional.get().getScpStorage().getHost());
        assertEquals(port, scpResourceOptional.get().getScpStorage().getPort());
        assertEquals(storageId, scpResourceOptional.get().getScpStorage().getStorageId());
        assertEquals(String.valueOf(user), scpResourceOptional.get().getScpStorage().getUser());
    }

    @Test
    public void testGetSCPResource_IncompleteSCPResource() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPResourceGetRequest scpResourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        SCPResourceRepository scpResourceRepository = Mockito.mock(SCPResourceRepository.class);
        SCPResourceEntity scpResourceEntity = Mockito.spy(new SCPResourceEntity());

        SCPStorageEntity scpStorageEntity = new SCPStorageEntity();

        String host = "host";
        int port = 8080;
        int user = 1;

        scpStorageEntity.setHost(host);
        scpStorageEntity.setPort(port);
        scpStorageEntity.setUser(user);

        String resourceId = "resourceId";
        String resourcePath = "resourcePath";
        String scpStorageId = "storageId";

        scpResourceEntity.setResourceId(resourceId);
        scpResourceEntity.setResourcePath(resourcePath);
        scpResourceEntity.setScpStorage(scpStorageEntity);
        scpResourceEntity.setScpStorageId(scpStorageId);

        Mockito.doReturn(scpResourceRepository).when(sqlResourceBackend).getScpResourceRepository();
        Mockito.when(scpResourceRepository.findByResourceId(null)).thenReturn(Optional.of(scpResourceEntity));

        assertThrows(MappingException.class ,() -> sqlResourceBackend.getSCPResource(scpResourceGetRequest));
    }

    @Test
    public void testGetSCPResource_IncompleteSCPStorageInSCPResource() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPResourceGetRequest scpResourceGetRequest = Mockito.mock(SCPResourceGetRequest.class);
        SCPResourceRepository scpResourceRepository = Mockito.mock(SCPResourceRepository.class);
        SCPResourceEntity scpResourceEntity = Mockito.spy(new SCPResourceEntity());

        SCPStorageEntity scpStorageEntity = new SCPStorageEntity();

        String host = "host";
        int port = 8080;
        String storageId = "randomStorageId";
        int user = 1;

        scpStorageEntity.setHost(host);
        scpStorageEntity.setPort(port);
        scpStorageEntity.setStorageId(storageId);
        scpStorageEntity.setUser(user);

        String resourceId = "resourceId";
        String scpStorageId = "storageId";

        scpResourceEntity.setResourceId(resourceId);
        scpResourceEntity.setScpStorage(scpStorageEntity);
        scpResourceEntity.setScpStorageId(scpStorageId);

        Mockito.doReturn(scpResourceRepository).when(sqlResourceBackend).getScpResourceRepository();
        Mockito.when(scpResourceRepository.findByResourceId(null)).thenReturn(Optional.of(scpResourceEntity));

        assertThrows(MappingException.class ,() -> sqlResourceBackend.getSCPResource(scpResourceGetRequest));
    }

    @Test
    public void testCreateSCPResource_IncompleteSCPCreateRequest() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        SCPStorageCreateRequest scpStorageCreateRequest = Mockito.mock(SCPStorageCreateRequest.class);
        SCPStorageRepository scpStorageRepository = Mockito.mock(SCPStorageRepository.class);

        String host = "host";
        int port = 8080;
        int user = 1;

        Mockito.when(scpStorageCreateRequest.getHost()).thenReturn(host);

        Mockito.when(scpStorageRepository.save(Mockito.any(SCPStorageEntity.class))).thenAnswer(invocation -> {
            SCPStorageEntity entity = invocation.getArgument(0);
            entity.setUser(user);
            entity.setPort(port);
            return entity;
        });
        Mockito.when(sqlResourceBackend.getScpStorageRepository()).thenReturn(scpStorageRepository);

        assertThrows(MappingException.class, () -> sqlResourceBackend.createSCPStorage(scpStorageCreateRequest));
    }

    @Test
    public void testGetLocalStorage() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        LocalResourceGetRequest localResourceGetRequest = Mockito.mock(LocalResourceGetRequest.class);
        LocalResourceRepository localResourceRepository = Mockito.mock( LocalResourceRepository.class);
        LocalResourceEntity localResourceEntity = new LocalResourceEntity();

        String resourceId = "resourceId";
        String resourcePath = "resourcePath";

        localResourceEntity.setResourceId(resourceId);
        localResourceEntity.setResourcePath(resourcePath);

        Mockito.doReturn(localResourceRepository).when(sqlResourceBackend).getLocalResourceRepository();
        Mockito.when(localResourceRepository.findByResourceId(null)).thenReturn(Optional.of(localResourceEntity));

        Optional<LocalResource> localResourceOptional = sqlResourceBackend.getLocalResource(localResourceGetRequest);
        assertTrue(localResourceOptional.isPresent());
        assertEquals(resourceId, localResourceOptional.get().getResourceId());
        assertEquals(resourcePath, localResourceOptional.get().getResourcePath());
    }

    @Test
    public void testGetLocalStorage_IncompleteLocalResouce() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        LocalResourceGetRequest localResourceGetRequest = Mockito.mock(LocalResourceGetRequest.class);
        LocalResourceRepository localResourceRepository = Mockito.mock( LocalResourceRepository.class);
        LocalResourceEntity localResourceEntity = new LocalResourceEntity();

        String resourceId = "resourceId";

        localResourceEntity.setResourceId(resourceId);

        Mockito.doReturn(localResourceRepository).when(sqlResourceBackend).getLocalResourceRepository();
        Mockito.when(localResourceRepository.findByResourceId(null)).thenReturn(Optional.of(localResourceEntity));

        assertThrows(MappingException.class,() -> sqlResourceBackend.getLocalResource(localResourceGetRequest));
    }

    @Test
    public void testCreateLocalResource() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        LocalResourceCreateRequest localResourceGetRequest = Mockito.mock(LocalResourceCreateRequest.class);
        LocalResourceRepository localResourceRepository = Mockito.mock( LocalResourceRepository.class);

        String resourceId = "resourceId";
        String resourcePath = "resourcePath";

        Mockito.when(localResourceRepository.save(Mockito.any(LocalResourceEntity.class))).thenAnswer(invocation -> {
            LocalResourceEntity entity = invocation.getArgument(0);
            entity.setResourcePath(resourcePath);
            entity.setResourceId(resourceId);
            return entity;
        });
        Mockito.when(sqlResourceBackend.getLocalResourceRepository()).thenReturn(localResourceRepository);

        LocalResource localResource = sqlResourceBackend.createLocalResource(localResourceGetRequest);
        assertEquals(resourceId, localResource.getResourceId());
        assertEquals(resourcePath, localResource.getResourcePath());
    }

    @Test
    public void testCreateLocalResource_IncompleteSCPCreateRequest() {
        SQLResourceBackend sqlResourceBackend = Mockito.spy(new SQLResourceBackend());
        LocalResourceCreateRequest localResourceGetRequest = Mockito.mock(LocalResourceCreateRequest.class);
        LocalResourceRepository localResourceRepository = Mockito.mock( LocalResourceRepository.class);

        String resourceId = "resourceId";

        Mockito.when(localResourceRepository.save(Mockito.any(LocalResourceEntity.class))).thenAnswer(invocation -> {
            LocalResourceEntity entity = invocation.getArgument(0);
            entity.setResourceId(resourceId);
            return entity;
        });
        Mockito.when(sqlResourceBackend.getLocalResourceRepository()).thenReturn(localResourceRepository);

        assertThrows(MappingException.class, () -> sqlResourceBackend.createLocalResource(localResourceGetRequest));
    }
}
