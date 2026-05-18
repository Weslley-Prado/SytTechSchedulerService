package com.syttech.syttech.scheduler.scheduler.domain.command;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Command input for RescheduleAppointmentUseCase (one of the fields filled). */
public record RescheduleCommand(UUID holdId, OffsetDateTime start) {}
