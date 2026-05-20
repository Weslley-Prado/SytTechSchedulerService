package com.syttech.syttech.scheduler.scheduler.adapter.input.web.security;

import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.ports.out.TokenVerifierPort;

import org.springframework.stereotype.Component;

/** Web-layer adapter over {@link TokenVerifierPort} for the auth filter. */
@Component
public class JwtTokenVerifier {

    private final TokenVerifierPort verifier;

    public JwtTokenVerifier(final TokenVerifierPort verifier) {
        this.verifier = verifier;
    }

    public Optional<UUID> verifyAccess(final String token) {
        return verifier.verifyAccess(token);
    }
}
