package com.syttech.syttech.scheduler.scheduler.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Raised when an Appointment is rescheduled. */
public record AppointmentRescheduledEvent(
        UUID appointmentId, OffsetDateTime previousStart, OffsetDateTime newStart) {}
