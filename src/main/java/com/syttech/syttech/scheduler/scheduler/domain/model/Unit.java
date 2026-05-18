package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.util.List;
import java.util.UUID;

/** Unit/salon offering services. */
public record Unit(
        UUID id,
        String name,
        String address,
        String city,
        String phone,
        String email,
        String coverImageUrl,
        List<BusinessHour> businessHours,
        boolean active) {}
