package com.syttech.syttech.scheduler.scheduler.adapter.output.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for the JWT issuer. */
@ConfigurationProperties(prefix = "syttech.security.jwt")
public class JwtProperties {

    /** HMAC-SHA256 secret (min 32 chars). */
    private String secret = "change-me-change-me-change-me-change-me-change-me";

    /** Access token TTL in seconds. */
    private long accessTtlSeconds = 3600L;

    /** Issuer claim. */
    private String issuer = "syttech.scheduler";

    public String getSecret() {
        return secret;
    }

    public void setSecret(final String secret) {
        this.secret = secret;
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public void setAccessTtlSeconds(final long accessTtlSeconds) {
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }
}
