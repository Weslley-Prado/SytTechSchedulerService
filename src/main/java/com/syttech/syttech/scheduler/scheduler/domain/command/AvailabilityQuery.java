package com.syttech.syttech.scheduler.scheduler.domain.command;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Query input for GetAvailabilityUseCase. professionalId may be null (any). */
public record AvailabilityQuery(
        UUID unitId,
        UUID serviceId,
        UUID professionalId,
        OffsetDateTime from,
        OffsetDateTime to,
        String timezone) {}
