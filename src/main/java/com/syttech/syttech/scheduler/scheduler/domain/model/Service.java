package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/** Concrete service that can be booked (e.g. Men's haircut). */
public record Service(
        UUID id,
        UUID categoryId,
        String name,
        String description,
        int durationMinutes,
        BigDecimal price,
        String currency,
        boolean active) {}
