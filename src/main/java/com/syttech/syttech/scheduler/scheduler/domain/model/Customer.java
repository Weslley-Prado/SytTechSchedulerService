package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Customer account. */
public record Customer(
        UUID id,
        String fullName,
        String email,
        String phone,
        String passwordHash,
        boolean emailVerified,
        String emailVerifyToken,
        OffsetDateTime emailVerifyExpiresAt,
        OffsetDateTime createdAt) {}
