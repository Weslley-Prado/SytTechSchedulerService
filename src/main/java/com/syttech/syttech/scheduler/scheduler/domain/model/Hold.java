package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Temporary pre-reservation with TTL. */
public record Hold(
        UUID id,
        UUID unitId,
        UUID serviceId,
        UUID professionalId,
        OffsetDateTime start,
        OffsetDateTime end,
        OffsetDateTime expiresAt,
        boolean consumed) {}
