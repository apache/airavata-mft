package org.apache.airavata.mft.secret.server.backend.custos;

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

import java.util.Map;
import java.util.Optional;

/**
 * Handle Custos secret management operations
 */
public class CustosSecretBackend implements SecretBackend {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustosSecretBackend.class);


    @Autowired
    private AgentAuthenticationHandler handler;

    @Autowired
    private CustosClientProvider clientProvider;

    @Value("${custos.id}")
    private String custosId;

    private ResourceSecretManagementAgentClient csAgentClient;

    private ResourceSecretManagementClient csClient;

    private IdentityManagementClient identityClient;


    @Override
    public void init() {
        try {
            csAgentClient = (ResourceSecretManagementAgentClient) clientProvider
                    .getResourceSecretManagementClientForAgents();
            csClient = clientProvider.getResourceSecretManagementClient();
            identityClient = clientProvider.getIdentityManagementClient();
        } catch (Exception ex) {
            LOGGER.error("Custos client initialization failed ", ex);
        }

    }

    @Override
    public void destroy() {

    }

    @Override
    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception {
        String agentId = request.getAuthzToken().getAgentId();
        String secret = request.getAuthzToken().getAgentSecret();
        Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
        if (optionalAuthConfig.isPresent()) {
            AuthConfig authConfig = optionalAuthConfig.get();
            SSHCredential sshCredential = csAgentClient.
                    getSSHCredential(request.getAuthzToken().getToken(),
                            authConfig.getAccessToken(), request.getSecretId(), false);
            SCPSecret scpSecret = SCPSecret.newBuilder()
                    .setSecretId(sshCredential.getMetadata().getToken())
                    .setPublicKey(sshCredential.getPublicKey())
                    .setPassphrase(sshCredential.getPassphrase())
                    .setPrivateKey(sshCredential.getPrivateKey()).build();
            return Optional.of(scpSecret);

        } else if (!request.getAuthzToken().getToken().isEmpty()) {
            if (identityClient.isAuthenticated(request.getAuthzToken().getToken())) {
                SSHCredential sshCredential = csClient.getSSHCredential(custosId, request.getSecretId(), false);
                SCPSecret scpSecret = SCPSecret.newBuilder()
                        .setSecretId(sshCredential.getMetadata().getToken())
                        .setPublicKey(sshCredential.getPublicKey())
                        .setPassphrase(sshCredential.getPassphrase())
                        .setPrivateKey(sshCredential.getPrivateKey()).build();
                return Optional.of(scpSecret);
            }
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
        String agentId = request.getAuthzToken().getAgentId();
        String secret = request.getAuthzToken().getAgentSecret();
        Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
        if (optionalAuthConfig.isPresent()) {
            AuthConfig authConfig = optionalAuthConfig.get();
            CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getToken(),
                    authConfig.getAccessToken(), custosId, request.getSecretId());
            Map<String, String> secretValues = credentialMap.getCredentialMapMap();
            S3Secret s3Secret = S3Secret.newBuilder()
                    .setSecretId(secretValues.get("secretId"))
                    .setAccessKey(secretValues.get("accessKey"))
                    .setSecretKey(secretValues.get("secretKey")).build();
            return Optional.of(s3Secret);

        } else if (!request.getAuthzToken().getToken().isEmpty()) {
            if (identityClient.isAuthenticated(request.getAuthzToken().getToken())) {
                CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getAuthzToken().getToken());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                S3Secret s3Secret = S3Secret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setAccessKey(secretValues.get("accessKey"))
                        .setSecretKey(secretValues.get("secretKey")).build();
                return Optional.of(s3Secret);
            }
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
        String agentId = request.getAuthzToken().getAgentId();
        String secret = request.getAuthzToken().getAgentSecret();
        Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
        if (optionalAuthConfig.isPresent()) {
            AuthConfig authConfig = optionalAuthConfig.get();
            CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getToken(),
                    authConfig.getAccessToken(), custosId, request.getSecretId());
            Map<String, String> secretValues = credentialMap.getCredentialMapMap();
            BoxSecret boxSecret = BoxSecret.newBuilder()
                    .setSecretId(secretValues.get("secretId"))
                    .setAccessToken(secretValues.get("accessToken")).build();
            return Optional.of(boxSecret);

        } else if (!request.getAuthzToken().getToken().isEmpty()) {
            if (identityClient.isAuthenticated(request.getAuthzToken().getToken())) {
                CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getAuthzToken().getToken());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                BoxSecret boxSecret = BoxSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setAccessToken(secretValues.get("accessToken")).build();
                return Optional.of(boxSecret);
            }
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
        String agentId = request.getAuthzToken().getAgentId();
        String secret = request.getAuthzToken().getAgentSecret();
        Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
        if (optionalAuthConfig.isPresent()) {
            AuthConfig authConfig = optionalAuthConfig.get();
            CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getToken(),
                    authConfig.getAccessToken(), custosId, request.getSecretId());
            Map<String, String> secretValues = credentialMap.getCredentialMapMap();
            AzureSecret azureSecret = AzureSecret.newBuilder()
                    .setSecretId(secretValues.get("secretId"))
                    .setConnectionString(secretValues.get("connectionString")).build();

            return Optional.of(azureSecret);

        } else if (!request.getAuthzToken().getToken().isEmpty()) {
            if (identityClient.isAuthenticated(request.getAuthzToken().getToken())) {
                CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getAuthzToken().getToken());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                AzureSecret azureSecret = AzureSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setConnectionString(secretValues.get("connectionString")).build();

                return Optional.of(azureSecret);
            }
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
        String agentId = request.getAuthzToken().getAgentId();
        String secret = request.getAuthzToken().getAgentSecret();
        Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
        if (optionalAuthConfig.isPresent()) {
            AuthConfig authConfig = optionalAuthConfig.get();
            CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getToken(),
                    authConfig.getAccessToken(), custosId, request.getSecretId());
            Map<String, String> secretValues = credentialMap.getCredentialMapMap();
            GCSSecret gcsSecret = GCSSecret.newBuilder()
                    .setSecretId(secretValues.get("secretId"))
                    .setCredentialsJson(secretValues.get("credentialsJson")).build();

            return Optional.of(gcsSecret);

        } else if (!request.getAuthzToken().getToken().isEmpty()) {
            if (identityClient.isAuthenticated(request.getAuthzToken().getToken())) {
                CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getAuthzToken().getToken());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                GCSSecret gcsSecret = GCSSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setCredentialsJson(secretValues.get("credentialsJson")).build();

                return Optional.of(gcsSecret);
            }
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
        String agentId = request.getAuthzToken().getAgentId();
        String secret = request.getAuthzToken().getAgentSecret();
        Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
        if (optionalAuthConfig.isPresent()) {
            AuthConfig authConfig = optionalAuthConfig.get();
            CredentialMap credentialMap = csAgentClient.getCredentialMap(request.getAuthzToken().getToken(),
                    authConfig.getAccessToken(), custosId, request.getSecretId());
            Map<String, String> secretValues = credentialMap.getCredentialMapMap();
            DropboxSecret dropboxSecret = DropboxSecret.newBuilder()
                    .setSecretId(secretValues.get("secretId"))
                    .setAccessToken(secretValues.get("accessToken")).build();

            return Optional.of(dropboxSecret);

        } else if (!request.getAuthzToken().getToken().isEmpty()) {
            if (identityClient.isAuthenticated(request.getAuthzToken().getToken())) {
                CredentialMap credentialMap = csClient.getCredentialMap(custosId, request.getAuthzToken().getToken());
                Map<String, String> secretValues = credentialMap.getCredentialMapMap();
                DropboxSecret dropboxSecret = DropboxSecret.newBuilder()
                        .setSecretId(secretValues.get("secretId"))
                        .setAccessToken(secretValues.get("accessToken")).build();

                return Optional.of(dropboxSecret);
            }
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
        String agentId = request.getAuthzToken().getAgentId();
        String secret = request.getAuthzToken().getAgentSecret();
        Optional<AuthConfig> optionalAuthConfig = handler.authenticate(agentId, secret);
        if (optionalAuthConfig.isPresent()) {
            AuthConfig authConfig = optionalAuthConfig.get();
            PasswordCredential passwordCredential = csAgentClient.getPasswordCredential(request.getAuthzToken().getToken(),
                    authConfig.getAccessToken(), custosId, request.getSecretId());
            FTPSecret ftpSecret = FTPSecret.newBuilder()
                    .setSecretId(request.getSecretId())
                    .setPassword(passwordCredential.getPassword())
                    .setUserId(passwordCredential.getUserId())
                    .build();

            return Optional.of(ftpSecret);

        } else if (!request.getAuthzToken().getToken().isEmpty()) {
            if (identityClient.isAuthenticated(request.getAuthzToken().getToken())) {
                PasswordCredential passwordCredential = csClient.getPasswordCredential(custosId, request.getAuthzToken().getToken());
                FTPSecret ftpSecret = FTPSecret.newBuilder()
                        .setSecretId(request.getSecretId())
                        .setPassword(passwordCredential.getPassword())
                        .setUserId(passwordCredential.getUserId())
                        .build();
                return Optional.of(ftpSecret);
            }
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
}
