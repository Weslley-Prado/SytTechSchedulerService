package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.util.UUID;

/** Professional executing services at a Unit. */
public record Professional(
        UUID id, UUID unitId, String name, String avatarUrl, Float rating, boolean active) {}
