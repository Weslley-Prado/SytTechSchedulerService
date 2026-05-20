package com.syttech.syttech.scheduler.scheduler.adapter.input.web.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Opportunistic JWT auth filter: if an {@code Authorization: Bearer …} header is present and valid,
 * sets the {@code AUTH_CUSTOMER_ID_ATTR} request attribute. Endpoints that need authentication call
 * {@code CurrentCustomer.requireId(request)}; public endpoints are untouched.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Request attribute holding the authenticated customer's UUID. */
    public static final String AUTH_CUSTOMER_ID_ATTR = "syttech.auth.customerId";

    private final JwtTokenVerifier verifier;

    public JwtAuthenticationFilter(final JwtTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = header.substring(7).trim();
            verifier.verifyAccess(token)
                    .ifPresent(id -> request.setAttribute(AUTH_CUSTOMER_ID_ATTR, id));
        }
        chain.doFilter(request, response);
    }
}
