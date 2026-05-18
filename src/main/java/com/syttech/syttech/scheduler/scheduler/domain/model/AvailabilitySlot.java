package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Bookable time window returned by the availability query. */
public record AvailabilitySlot(OffsetDateTime start, OffsetDateTime end, UUID professionalId) {}
