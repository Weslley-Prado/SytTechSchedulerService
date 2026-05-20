package com.syttech.syttech.scheduler.scheduler.adapter.output.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.syttech.syttech.scheduler.scheduler.domain.command.LoginResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.ports.out.TokenIssuerPort;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/** Minimal HS256 JWT issuer (no external deps). */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class HmacJwtTokenIssuerAdapter implements TokenIssuerPort {

    private static final Base64.Encoder URL_ENC = Base64.getUrlEncoder().withoutPadding();

    private final JwtProperties props;

    public HmacJwtTokenIssuerAdapter(final JwtProperties props) {
        this.props = props;
    }

    @Override
    public LoginResult issueFor(final Customer customer) {
        long now = Instant.now().getEpochSecond();
        long exp = now + props.getAccessTtlSeconds();
        String access = jwt(customer.id(), customer.email(), now, exp, "access");
        String refresh =
                jwt(
                        customer.id(),
                        customer.email(),
                        now,
                        now + props.getAccessTtlSeconds() * 24L,
                        "refresh");
        return new LoginResult(
                customer.id(),
                customer.fullName(),
                customer.email(),
                customer.emailVerified(),
                access,
                refresh,
                props.getAccessTtlSeconds());
    }

    private String jwt(
            final UUID sub, final String email, final long iat, final long exp, final String type) {
        String header = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload =
                b64(
                        String.format(
                                "{\"iss\":\"%s\",\"sub\":\"%s\",\"email\":\"%s\","
                                        + "\"type\":\"%s\",\"iat\":%d,\"exp\":%d}",
                                props.getIssuer(), sub, email, type, iat, exp));
        String signingInput = header + "." + payload;
        String signature = b64Sig(signingInput);
        return signingInput + "." + signature;
    }

    private String b64(final String s) {
        return URL_ENC.encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String b64Sig(final String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(
                    new SecretKeySpec(
                            props.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENC.encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}
