package org.apache.airavata.mft.agent;

import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;
import org.apache.airavata.mft.admin.MFTConsulClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MFTAgentTest {

    @Autowired
    @InjectMocks
    private MFTAgent mftAgent;

    @Mock
    private MFTConsulClient mftConsulClient;

    @Mock
    private ExecutorService transferRequestExecutor;

    @Mock
    private ConsulCache.Listener<String, Value> transferCacheListener;

    @Mock
    private ConsulCache.Listener<String, Value> rpcCacheListener;

    @Mock
    private KVCache rpcMessageCache;

    @Mock
    private KVCache transferMessageCache;

    // Mocking a static method using Mockito.mockStatic
    @Test
    void testInit() {
        try (MockedStatic<KVCache> kvCache = Mockito.mockStatic(KVCache.class);
             MockedStatic<Executors> transferRequestExecutor = Mockito.mockStatic(Executors.class)) {
            // given
            kvCache.when(() -> KVCache.newCache(
                    any(KeyValueClient.class),
                    any(String.class))).thenReturn(null);
            transferRequestExecutor.when(() -> Executors.newFixedThreadPool(any(Integer.class)))
                    .thenReturn(null);

            // when
            mftAgent.init();

            //then
            verify(mftConsulClient, times(2)).getKvClient();
        }
    }

    @Test
    void testDisconnectAgent() {
        mftAgent.disconnectAgent();
        verify(rpcMessageCache, times(1)).removeListener(any());
    }

    // Mocking a method in the same class using Mockito.spy
    @Test
    void testStop(){
        // given
        MFTAgent mftAgent1 = spy(mftAgent);
        doNothing().when(mftAgent1).disconnectAgent();
        doNothing().when(transferRequestExecutor).shutdown();

        // when
        mftAgent1.stop();

        // then
        verify(mftAgent1, times(1)).disconnectAgent();
        verify(transferRequestExecutor, times(1)).shutdown();
    }

    // Mock nested methods
    @Test
    void testEstablishConnectionWithConsul() throws Exception{
//        ImmutableSession sessionObj = ImmutableSession.builder().name("testagent").behavior("delete").ttl("10s").build();
//        try (MockedStatic<ImmutableSession> session = Mockito.mockStatic(ImmutableSession.class)) {
//            // given
//            SessionCreatedResponse sessResp = new SessionCreatedResponse() {
//                @Override
//                public String getId() {
//                    return "test-id";
//                }
//            };
//            MFTAgent mftAgent1 = spy(mftAgent);
//            MFTConsulClient mftConsulClient1 = mock(MFTConsulClient.class, RETURNS_DEEP_STUBS);
//            session.when(() -> ImmutableSession.builder().name(anyString()).behavior(anyString()).ttl(anyString()).build()).thenReturn(sessionObj);
//            when(mftConsulClient1.getSessionClient().createSession(any(ImmutableSession.class))).thenReturn(sessResp);
//            when(mftConsulClient1.getKvClient().acquireLock(anyString(), anyString())).thenReturn(true);
//            doNothing().when(mftAgent1).init();
//
//            // when
//            mftAgent1.start();
//
//            // then
//            verify(mftConsulClient).submitTransferStateToProcess(anyString(), anyString(), any(TransferState.class));
//        }
    }

    @Test
    void testRunMFTAgent() throws Exception{
    }
}