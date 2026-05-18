package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.util.UUID;

/** Service category exposed by a Unit (e.g. Hair, Beard). */
public record Category(UUID id, UUID unitId, String name, String iconUrl) {}
