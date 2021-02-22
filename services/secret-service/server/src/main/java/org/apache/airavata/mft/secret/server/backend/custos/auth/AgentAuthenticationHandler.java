package org.apache.airavata.mft.secret.server.backend.custos.auth;

import com.google.protobuf.Struct;
import org.apache.airavata.mft.secret.server.backend.custos.CustosException;
import org.apache.custos.clients.CustosClientProvider;
import org.apache.custos.identity.management.client.IdentityManagementClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handle agent authentication
 */
public class AgentAuthenticationHandler implements AuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentAuthenticationHandler.class);

    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private Map<String, AuthConfig> authCache = new ConcurrentHashMap();

    private String custosId;

    @Autowired
    private CustosClientProvider custosClientProvider;


    public AgentAuthenticationHandler(String custosId) {
        this.custosId = custosId;
    }

    @Override
    public Optional<AuthConfig> authenticate(String id, String secret) throws Exception {
        try {
            AuthConfig cachedAuthConfig = authCache.get(id);
            AuthConfig authConfig = new AuthConfig();
            final boolean agentRequest = id != null & secret != null & !id.isEmpty() & !secret.isEmpty();
            if (cachedAuthConfig == null && agentRequest) {
                IdentityManagementClient identityManagementClient = custosClientProvider.getIdentityManagementClient();
                Struct tokenResponse = identityManagementClient.getAgentToken(custosId, id, secret,
                        CLIENT_CREDENTIALS, null);

                if (tokenResponse.getFieldsMap() != null && !tokenResponse.getFieldsMap().isEmpty()) {
                    authConfig.setId(id);
                    authConfig.setSecret(secret);
                    tokenResponse.getFieldsMap().keySet().forEach(key -> {
                        String value = tokenResponse.getFieldsMap().get(key).getStringValue();
                        if (key.trim().equals("access_token")) {
                            authConfig.setAccessToken(value);
                        } else if (key.trim().equals("refresh_token")) {
                            authConfig.setRefreshToken(value);
                        } else if (key.trim().equals("id_token")) {
                            authConfig.setIdToken(value);
                        }
                    });
                    authCache.put(id, authConfig);
                    return Optional.of(authConfig);
                }
            } else if (id != null && agentRequest) {
                return Optional.of(cachedAuthConfig);

            }
            return Optional.empty();

        } catch (Exception ex) {
            String errorMsg = "Error occurred while authenticating agent  " + id + " with Custos";
            LOGGER.error(errorMsg + ", reason :" + ex.getMessage());
            throw new CustosException(errorMsg, ex);
        }

    }

}
