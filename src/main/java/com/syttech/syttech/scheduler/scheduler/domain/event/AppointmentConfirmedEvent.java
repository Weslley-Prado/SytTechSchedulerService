package com.syttech.syttech.scheduler.scheduler.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Raised when a Hold is converted into a confirmed Appointment. */
public record AppointmentConfirmedEvent(
        UUID appointmentId,
        String code,
        UUID customerId,
        String customerEmail,
        OffsetDateTime start) {}
