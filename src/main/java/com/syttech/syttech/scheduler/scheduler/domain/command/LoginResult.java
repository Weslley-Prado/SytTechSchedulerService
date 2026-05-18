package com.syttech.syttech.scheduler.scheduler.domain.command;

import java.util.UUID;

/** Result of LoginUseCase. */
public record LoginResult(
        UUID customerId,
        String fullName,
        String email,
        boolean emailVerified,
        String accessToken,
        String refreshToken,
        long expiresIn) {}
