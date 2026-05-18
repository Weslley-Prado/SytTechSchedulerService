package com.syttech.syttech.scheduler.scheduler.domain.command;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Command input for CreateHoldUseCase. */
public record CreateHoldCommand(
        UUID unitId, UUID serviceId, UUID professionalId, OffsetDateTime start) {}
