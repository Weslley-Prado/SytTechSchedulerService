package com.syttech.syttech.scheduler.scheduler.adapter.output.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.syttech.syttech.scheduler.scheduler.ports.out.TokenVerifierPort;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/** Verifies HS256 JWTs produced by {@link HmacJwtTokenIssuerAdapter}. */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class HmacJwtTokenVerifierAdapter implements TokenVerifierPort {

    private static final Base64.Decoder URL_DEC = Base64.getUrlDecoder();
    private static final Base64.Encoder URL_ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Pattern SUB_RX = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern EXP_RX = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");
    private static final Pattern TYPE_RX = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");

    private final JwtProperties props;

    public HmacJwtTokenVerifierAdapter(final JwtProperties props) {
        this.props = props;
    }

    @Override
    public Optional<UUID> verifyAccess(final String token) {
        return verifyOfType(token, "access");
    }

    @Override
    public Optional<UUID> verifyRefresh(final String token) {
        return verifyOfType(token, "refresh");
    }

    private Optional<UUID> verifyOfType(final String token, final String expectedType) {
        if (token == null) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        String expectedSig = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expectedSig, parts[2])) {
            return Optional.empty();
        }
        String payload;
        try {
            payload = new String(URL_DEC.decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        Matcher typeM = TYPE_RX.matcher(payload);
        if (!typeM.find() || !expectedType.equals(typeM.group(1))) {
            return Optional.empty();
        }
        Matcher expM = EXP_RX.matcher(payload);
        if (expM.find() && Long.parseLong(expM.group(1)) < Instant.now().getEpochSecond()) {
            return Optional.empty();
        }
        Matcher subM = SUB_RX.matcher(payload);
        if (!subM.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(subM.group(1)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String sign(final String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(
                    new SecretKeySpec(
                            props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENC.encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to verify JWT", e);
        }
    }

    private static boolean constantTimeEquals(final String a, final String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
