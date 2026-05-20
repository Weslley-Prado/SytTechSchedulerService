package com.syttech.syttech.scheduler.scheduler.adapter.input.web.security;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

import com.syttech.syttech.scheduler.shared.kernel.UnauthenticatedException;

/** Utility to read the authenticated customer set by {@link JwtAuthenticationFilter}. */
public final class CurrentCustomer {

    private CurrentCustomer() {}

    /** Returns the customerId or throws 401 if missing. */
    public static UUID requireId(final HttpServletRequest request) {
        Object value = request.getAttribute(JwtAuthenticationFilter.AUTH_CUSTOMER_ID_ATTR);
        if (value instanceof UUID id) {
            return id;
        }
        throw new UnauthenticatedException("Authentication required");
    }

    /** Returns the customerId or {@code null} if anonymous. */
    public static UUID optionalId(final HttpServletRequest request) {
        Object value = request.getAttribute(JwtAuthenticationFilter.AUTH_CUSTOMER_ID_ATTR);
        return value instanceof UUID id ? id : null;
    }
}
