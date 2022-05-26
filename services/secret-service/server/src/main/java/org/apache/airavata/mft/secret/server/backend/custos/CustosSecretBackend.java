package org.apache.airavata.mft.secret.server.backend.custos;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.airavata.datalake.drms.AuthCredentialType;
import org.apache.airavata.datalake.drms.AuthenticatedUser;
import org.apache.airavata.datalake.drms.DRMSServiceAuthToken;
import org.apache.airavata.datalake.drms.storage.*;
import org.apache.airavata.datalake.drms.storage.preference.ssh.SSHStoragePreference;
import org.apache.airavata.mft.common.AuthToken;
import org.apache.airavata.mft.common.DelegateAuth;
import org.apache.airavata.mft.credential.stubs.azure.*;
import org.apache.airavata.mft.credential.stubs.box.*;
import org.apache.airavata.mft.credential.stubs.dropbox.*;
import org.apache.airavata.mft.credential.stubs.ftp.*;
import org.apache.airavata.mft.credential.stubs.gcs.*;
import org.apache.airavata.mft.credential.stubs.s3.*;
import org.apache.airavata.mft.credential.stubs.scp.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.server.backend.custos.auth.AgentAuthenticationHandler;
import org.apache.airavata.mft.secret.server.backend.custos.auth.AuthConfig;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.identity.management.client.IdentityManagementClient;
import org.apache.custos.resource.secret.management.client.ResourceSecretManagementAgentClient;
import org.apache.custos.resource.secret.management.client.ResourceSecretManagementClient;
import org.apache.custos.resource.secret.service.CredentialMap;
import org.apache.custos.resource.secret.service.PasswordCredential;
import org.apache.custos.resource.secret.service.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Handle Custos secret management operations
 */
public class CustosSecretBackend implements SecretBackend {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustosSecretBackend.class);

    @Value("${custos.host}")
    private String custosHost;

    @Value("${custos.port}")
    private int custosPort;

    @Value("${custos.id}")
    private String custosId;

    @Value("${custos.secret}")
    private String custosSecret;

    @Value("${custos.backend.drms.host}")
    private String drmsHost;

    @Value("${custos.backend.drms.port}")
    private int drmsPort;

    private AgentAuthenticationHandler handler;

    private CustosClientsFactory custosClientsFactory;

    private IdentityManagementClient identityClient;

    private ResourceSecretManagementClient csClient;

    private ResourceSecretManagementAgentClient csAgentClient;


    @Override
    public void init() {
        try {

            custosClientsFactory = new CustosClientsFactory(custosHost, custosPort, custosId, custosSecret);
            handler = new AgentAuthenticationHandler(custosId, custosSecret, custosClientsFactory);
            Optional<CustosClientProvider> custosClientProvider = custosClientsFactory.getCustosClientProvider(custosId);
            if (custosClientProvider.isPresent()) {

                identityClient = custosClientProvider.get().getIdentityManagementClient();
                csClient = custosClientProvider.get().getResourceSecretManagementClient();
                csAgentClient = (ResourceSecretManagementAgentClient) custosClientProvider.get()
                        .getResourceSecretManagementClientForAgents();
            }

        } catch (Exception ex) {
            LOGGER.error("Custos client initialization failed ", ex);
        }
    }

    @Override
    public void destroy() {
        try {
            handler.close();
        } catch (IOException e) {
            LOGGER.error("Error while closing agents");
        } finally {
            this.custosClientsFactory = null;
            this.handler = null;
        }
    }

    private AnyStoragePreference getStoragePreference(String storagePefId) {
        return AnyStoragePreference.newBuilder().build();
    }

    private DRMSServiceAuthToken getDrmsToken(AuthToken authToken) {
        switch (authToken.getAuthMechanismCase()) {
            case USERTOKENAUTH:
                return DRMSServiceAuthToken.newBuilder().setAccessToken(authToken.getUserTokenAuth().getToken()).build();

            case DELEGATEAUTH:
                DelegateAuth delegateAuth = authToken.getDelegateAuth();
                return DRMSServiceAuthToken.newBuilder()
                        .setAccessToken(Base64.getEncoder()
                                .encodeToString((delegateAuth.getClientId() + ":" + delegateAuth.getClientSecret())
                                        .getBytes(StandardCharsets.UTF_8)))
                        .setAuthCredentialType(AuthCredentialType.AGENT_ACCOUNT_CREDENTIAL)
                        .setAuthenticatedUser(AuthenticatedUser.newBuilder()
                                .setUsername(delegateAuth.getUserId())
                                .setTenantId(delegateAuth.getPropertiesOrThrow("TENANT_ID"))
                                .build())
                        .build();
        }
        return null;
    }


    @Override
    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception {

        DRMSServiceAuthToken drmsToken = getDrmsToken(request.getAuthzToken());

        if (drmsToken == null) {
            LOGGER.error("DRMS Token can not be null");
            return Optional.empty();
        }

        String storagePrefId = request.getSecretId();

        ManagedChannel channel = ManagedChannelBuilder.forAddress(drmsHost, drmsPort).usePlaintext().build();
        AnyStoragePreference storagePreference;

        try {
            StoragePreferenceServiceGrpc.StoragePreferenceServiceBlockingStub spClient =
                    StoragePreferenceServiceGrpc.newBlockingStub(channel);

            StoragePreferenceFetchResponse storagePreferenceResp = spClient.
                    fetchStoragePreference(StoragePreferenceFetchRequest.newBuilder().
                            setAuthToken(drmsToken).setStoragePreferenceId(storagePrefId).build());

            storagePreference = storagePreferenceResp.getStoragePreference();
        } finally {
            channel.shutdown();
        }

        SSHStoragePreference sshStoragePreference;
        if (storagePreference.getStorageCase() == AnyStoragePreference.StorageCase.SSH_STORAGE_PREFERENCE) {
            sshStoragePreference = storagePreference.getSshStoragePreference();
        } else {
            LOGGER.error("Invalid storage case {} for preference {}", storagePreference.getStorageCase(), storagePrefId);
            return Optional.empty();
        }

        switch (request.getAuthzToken().getAuthMechanismCase()) {
            case AGENTAUTH:
                String agentId = request.getAuthzToken().getAgentAuth().getAgentId();
                String secret = request.getAuthzToken().getAgentAuth().getAgentSecret();

                Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
                if (optionalAuthConfig.isPresent()) {
                    AuthConfig authConfig = optionalAuthConfig.get();
                    SSHCredential sshCredential = csAgentClient.getSSHCredential(request.getAuthzToken().getAgentAuth().getToken(),
                            authConfig.getAccessToken(), sshStoragePreference.getCredentialToken(), false);
                    SCPSecret scpSecret = SCPSecret.newBuilder()
                            .setUser(sshStoragePreference.getUserName())
                            .setSecretId(sshCredential.getMetadata().getToken())
                            .setPublicKey(sshCredential.getPublicKey())
                            .setPassphrase(sshCredential.getPassphrase())
                            .setPrivateKey(sshCredential.getPrivateKey()).build();
                    return Optional.of(scpSecret);
                }
                break;
            case USERTOKENAUTH:
                if (identityClient.isAuthenticated(request.getAuthzToken().getUserTokenAuth().getToken())) {
                    //custosId need to be replaced with actual gateway custos Id
                    SSHCredential sshCredential = csClient.getSSHCredential(custosId, sshStoragePreference.getCredentialToken(), false);
                    SCPSecret scpSecret = SCPSecret.newBuilder()
                            .setUser(sshStoragePreference.getUserName())
                            .setSecretId(sshCredential.getMetadata().getToken())
                            .setPublicKey(sshCredential.getPublicKey())
                            .setPassphrase(sshCredential.getPassphrase())
                            .setPrivateKey(sshCredential.getPrivateKey()).build();
                    return Optional.of(scpSecret);
                }
                break;
            case DELEGATEAUTH:
                DelegateAuth delegateAuth = request.getAuthzToken().getDelegateAuth();
                ResourceSecretManagementClient csClient = getTenantResourceSecretManagementClient(delegateAuth);
                SSHCredential sshCredential = csClient.getSSHCredential(delegateAuth.getPropertiesMap().get("TENANT_ID"),
                        sshStoragePreference.getCredentialToken(), false);
                SCPSecret scpSecret = SCPSecret.newBuilder()
                        .setUser(sshStoragePreference.getUserName())
                        .setSecretId(sshCredential.getMetadata().getToken())
                        .setPublicKey(sshCredential.getPublicKey())
                        .setPassphrase(sshCredential.getPassphrase())
                        .setPrivateKey(sshCredential.getPrivateKey()).build();
                return Optional.of(scpSecret);
        }
        return Optional.empty();
    }

    @Override
    public SCPSecret createSCPSecret(SCPSecretCreateRequest request) {
        return null;
    }

    @Override
    public boolean updateSCPSecret(SCPSecretUpdateRequest request) {
        return false;
    }

    @Override
    public boolean deleteSCPSecret(SCPSecretDeleteRequest request) {
        return false;
    }

    @Override
    public Optional<S3Secret> getS3Secret(S3SecretGetRequest request) throws Exception {
        switch (request.getAuthzToken().getAuthMechanismCase()) {
            case AGENTAUTH:
                String agentId = request.getAuthzToken().getAgentAuth().getAgentId();
                String secret = request.getAuthzToken().getAgentAuth().getAgentSecret();
                Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
                if (optionalAuthConfig.isPresent()) {
                    AuthConfig authConfig = optionalAuthConfig.get();
                    CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getAgentAuth().getToken(),
                            authConfig.getAccessToken(), custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    S3Secret s3Secret = S3Secret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setAccessKey(secretValues.get("accessKey"))
                            .setSecretKey(secretValues.get("secretKey")).build();
                    return Optional.of(s3Secret);
                }
                break;
            case USERTOKENAUTH:
                if (identityClient.isAuthenticated(request.getAuthzToken().getUserTokenAuth().getToken())) {
                    CredentialMap credentialMap = csClient.getCredentialMap(custosId,
                            request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    S3Secret s3Secret = S3Secret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setAccessKey(secretValues.get("accessKey"))
                            .setSecretKey(secretValues.get("secretKey")).build();
                    return Optional.of(s3Secret);
                }
                break;
            case DELEGATEAUTH:
                DelegateAuth delegateAuth = request.getAuthzToken().getDelegateAuth();
                ResourceSecretManagementClient csClient = getTenantResourceSecretManagementClient(delegateAuth);
                CredentialMap credentialMap = csClient.getCredentialMap(delegateAuth.getPropertiesMap().get("TENANT_ID"),
                        request.getSecretId());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                S3Secret s3Secret = S3Secret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setAccessKey(secretValues.get("accessKey"))
                        .setSecretKey(secretValues.get("secretKey")).build();
                return Optional.of(s3Secret);
        }
        return Optional.empty();
    }

    @Override
    public S3Secret createS3Secret(S3SecretCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateS3Secret(S3SecretUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteS3Secret(S3SecretDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<BoxSecret> getBoxSecret(BoxSecretGetRequest request) throws Exception {

        switch (request.getAuthzToken().getAuthMechanismCase()) {
            case AGENTAUTH:
                String agentId = request.getAuthzToken().getAgentAuth().getAgentId();
                String secret = request.getAuthzToken().getAgentAuth().getAgentSecret();
                Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
                if (optionalAuthConfig.isPresent()) {
                    AuthConfig authConfig = optionalAuthConfig.get();
                    CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getAgentAuth().getToken(),
                            authConfig.getAccessToken(), custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    BoxSecret boxSecret = BoxSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setAccessToken(secretValues.get("accessToken")).build();
                    return Optional.of(boxSecret);
                }
                break;
            case USERTOKENAUTH:
                if (identityClient.isAuthenticated(request.getAuthzToken().getUserTokenAuth().getToken())) {
                    CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    BoxSecret boxSecret = BoxSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setAccessToken(secretValues.get("accessToken")).build();
                    return Optional.of(boxSecret);
                }
                break;
            case DELEGATEAUTH:
                DelegateAuth delegateAuth = request.getAuthzToken().getDelegateAuth();
                ResourceSecretManagementClient csClient = getTenantResourceSecretManagementClient(delegateAuth);
                CredentialMap credentialMap = csClient.getCredentialMap(delegateAuth.getPropertiesMap().get("TENANT_ID"),
                        request.getSecretId());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                BoxSecret boxSecret = BoxSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setAccessToken(secretValues.get("accessToken")).build();
                return Optional.of(boxSecret);

        }
        return Optional.empty();
    }

    @Override
    public BoxSecret createBoxSecret(BoxSecretCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateBoxSecret(BoxSecretUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteBoxSecret(BoxSecretDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<AzureSecret> getAzureSecret(AzureSecretGetRequest request) throws Exception {


        switch (request.getAuthzToken().getAuthMechanismCase()) {
            case AGENTAUTH:
                String agentId = request.getAuthzToken().getAgentAuth().getAgentId();
                String secret = request.getAuthzToken().getAgentAuth().getAgentSecret();
                Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
                if (optionalAuthConfig.isPresent()) {
                    AuthConfig authConfig = optionalAuthConfig.get();
                    CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getAgentAuth().getToken(),
                            authConfig.getAccessToken(), custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    AzureSecret azureSecret = AzureSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setConnectionString(secretValues.get("connectionString")).build();

                    return Optional.of(azureSecret);
                }
                break;
            case USERTOKENAUTH:
                if (identityClient.isAuthenticated(request.getAuthzToken().getUserTokenAuth().getToken())) {
                    CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getAuthzToken()
                            .getUserTokenAuth().getToken());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    AzureSecret azureSecret = AzureSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setConnectionString(secretValues.get("connectionString")).build();

                    return Optional.of(azureSecret);
                }
                break;
            case DELEGATEAUTH:
                DelegateAuth delegateAuth = request.getAuthzToken().getDelegateAuth();
                ResourceSecretManagementClient csClient = getTenantResourceSecretManagementClient(delegateAuth);
                CredentialMap credentialMap = csClient.getCredentialMap(delegateAuth.getPropertiesMap().get("TENANT_ID"),
                        request.getSecretId());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                AzureSecret azureSecret = AzureSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setConnectionString(secretValues.get("connectionString")).build();

                return Optional.of(azureSecret);
        }
        return Optional.empty();
    }

    @Override
    public AzureSecret createAzureSecret(AzureSecretCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateAzureSecret(AzureSecretUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteAzureSecret(AzureSecretDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<GCSSecret> getGCSSecret(GCSSecretGetRequest request) throws Exception {


        switch (request.getAuthzToken().getAuthMechanismCase()) {
            case AGENTAUTH:
                String agentId = request.getAuthzToken().getAgentAuth().getAgentId();
                String secret = request.getAuthzToken().getAgentAuth().getAgentSecret();
                Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
                if (optionalAuthConfig.isPresent()) {
                    AuthConfig authConfig = optionalAuthConfig.get();
                    CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getAgentAuth().getToken(),
                            authConfig.getAccessToken(), custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    GCSSecret gcsSecret = GCSSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setCredentialsJson(secretValues.get("credentialsJson")).build();

                    return Optional.of(gcsSecret);
                }
                break;
            case USERTOKENAUTH:
                if (identityClient.isAuthenticated(request.getAuthzToken().getUserTokenAuth().getToken())) {
                    CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    GCSSecret gcsSecret = GCSSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setCredentialsJson(secretValues.get("credentialsJson")).build();

                    return Optional.of(gcsSecret);
                }
                break;
            case DELEGATEAUTH:
                DelegateAuth delegateAuth = request.getAuthzToken().getDelegateAuth();
                ResourceSecretManagementClient csClient = getTenantResourceSecretManagementClient(delegateAuth);
                CredentialMap credentialMap = csClient.getCredentialMap(delegateAuth.getPropertiesMap().get("TENANT_ID"),
                        request.getSecretId());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                GCSSecret gcsSecret = GCSSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setCredentialsJson(secretValues.get("credentialsJson")).build();

                return Optional.of(gcsSecret);
        }
        return Optional.empty();
    }

    @Override
    public GCSSecret createGCSSecret(GCSSecretCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateGCSSecret(GCSSecretUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteGCSSecret(GCSSecretDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<DropboxSecret> getDropboxSecret(DropboxSecretGetRequest request) throws Exception {


        switch (request.getAuthzToken().getAuthMechanismCase()) {
            case AGENTAUTH:
                String agentId = request.getAuthzToken().getAgentAuth().getAgentId();
                String secret = request.getAuthzToken().getAgentAuth().getAgentSecret();
                Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
                if (optionalAuthConfig.isPresent()) {
                    AuthConfig authConfig = optionalAuthConfig.get();
                    CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getAgentAuth().getToken(),
                            authConfig.getAccessToken(), custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    DropboxSecret dropboxSecret = DropboxSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setAccessToken(secretValues.get("accessToken")).build();

                    return Optional.of(dropboxSecret);
                }
                break;
            case USERTOKENAUTH:
                if (identityClient.isAuthenticated(request.getAuthzToken().getUserTokenAuth().getToken())) {
                    CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getSecretId());
                    Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                    DropboxSecret dropboxSecret = DropboxSecret.newBuilder()
                            .setSecretId(secretValues.get("secretId"))
                            .setAccessToken(secretValues.get("accessToken")).build();

                    return Optional.of(dropboxSecret);
                }
                break;
            case DELEGATEAUTH:
                DelegateAuth delegateAuth = request.getAuthzToken().getDelegateAuth();
                ResourceSecretManagementClient csClient = getTenantResourceSecretManagementClient(delegateAuth);
                CredentialMap credentialMap = csClient.getCredentialMap(delegateAuth.getPropertiesMap().get("TENANT_ID"),
                        request.getSecretId());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                DropboxSecret dropboxSecret = DropboxSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setAccessToken(secretValues.get("accessToken")).build();

                return Optional.of(dropboxSecret);
        }
        return Optional.empty();
    }

    @Override
    public DropboxSecret createDropboxSecret(DropboxSecretCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateDropboxSecret(DropboxSecretUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteDropboxSecret(DropboxSecretDeleteRequest request) throws Exception {
        return false;
    }

    @Override
    public Optional<FTPSecret> getFTPSecret(FTPSecretGetRequest request) throws Exception {

        switch (request.getAuthzToken().getAuthMechanismCase()) {
            case AGENTAUTH:
                String agentId = request.getAuthzToken().getAgentAuth().getAgentId();
                String secret = request.getAuthzToken().getAgentAuth().getAgentSecret();
                Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
                if (optionalAuthConfig.isPresent()) {
                    AuthConfig authConfig = optionalAuthConfig.get();
                    PasswordCredential passwordCredential = csAgentClient.getPasswordCredential(request
                                    .getAuthzToken().getAgentAuth().getToken(),
                            authConfig.getAccessToken(), custosId, request.getSecretId());
                    FTPSecret ftpSecret = FTPSecret.newBuilder()
                            .setSecretId(request.getSecretId())
                            .setPassword(passwordCredential.getPassword())
                            .setUserId(passwordCredential.getUserId())
                            .build();

                    return Optional.of(ftpSecret);
                }
                break;
            case USERTOKENAUTH:
                if (identityClient.isAuthenticated(request.getAuthzToken().getAgentAuth().getToken())) {
                    PasswordCredential passwordCredential = csClient.getPasswordCredential(custosId,
                            request.getSecretId());
                    FTPSecret ftpSecret = FTPSecret.newBuilder()
                            .setSecretId(request.getSecretId())
                            .setPassword(passwordCredential.getPassword())
                            .setUserId(passwordCredential.getUserId())
                            .build();
                    return Optional.of(ftpSecret);
                }
                break;
            case DELEGATEAUTH:
                DelegateAuth delegateAuth = request.getAuthzToken().getDelegateAuth();
                // TODO validate delegate auth token
                ResourceSecretManagementClient csClient = getTenantResourceSecretManagementClient(delegateAuth);
                PasswordCredential passwordCredential = csClient
                        .getPasswordCredential(delegateAuth.getPropertiesMap().get("TENANT_ID"),
                                request.getSecretId());
                FTPSecret ftpSecret = FTPSecret.newBuilder()
                        .setSecretId(request.getSecretId())
                        .setPassword(passwordCredential.getPassword())
                        .setUserId(passwordCredential.getUserId())
                        .build();
                return Optional.of(ftpSecret);
        }
        return Optional.empty();
    }

    @Override
    public FTPSecret createFTPSecret(FTPSecretCreateRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean updateFTPSecret(FTPSecretUpdateRequest request) throws Exception {
        return false;
    }

    @Override
    public boolean deleteFTPSecret(FTPSecretDeleteRequest request) throws Exception {
        return false;
    }


    private ResourceSecretManagementClient getTenantResourceSecretManagementClient(DelegateAuth delegateAuth) throws IOException {
        CustosClientProvider custosClientProvider = custosClientsFactory
                .getCustosClientProvider(custosId, custosSecret);
        return custosClientProvider
                .getResourceSecretManagementClient();
    }
}
