package com.syttech.syttech.scheduler.scheduler.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Raised when an Appointment is cancelled. */
public record AppointmentCancelledEvent(
        UUID appointmentId, UUID customerId, OffsetDateTime cancelledAt) {}
