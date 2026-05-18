package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Confirmed appointment derived from a Hold. */
public record Appointment(
        UUID id,
        UUID unitId,
        UUID serviceId,
        UUID professionalId,
        UUID customerId,
        String code,
        AppointmentStatus status,
        OffsetDateTime start,
        OffsetDateTime end,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}
