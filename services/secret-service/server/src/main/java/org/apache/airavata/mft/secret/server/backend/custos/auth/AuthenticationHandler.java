package org.apache.airavata.mft.secret.server.backend.custos.auth;

import java.util.Optional;

/**
 * Represents the authentication related functional interfaces
 */
public interface AuthenticationHandler {


    Optional<AuthConfig> authenticate(String id, String secret) throws Exception;


}
