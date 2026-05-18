package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/** Weekly opening hours for a Unit. */
public record BusinessHour(
        UUID id, UUID unitId, DayOfWeek dayOfWeek, LocalTime opensAt, LocalTime closesAt) {}
