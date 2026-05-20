package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.util.Optional;
import java.util.UUID;

/** Verifies access and refresh tokens issued by {@link TokenIssuerPort}. */
public interface TokenVerifierPort {

    /** Returns the subject (customerId) if the token is a valid access token. */
    Optional<UUID> verifyAccess(String token);

    /** Returns the subject (customerId) if the token is a valid refresh token. */
    Optional<UUID> verifyRefresh(String token);
}
