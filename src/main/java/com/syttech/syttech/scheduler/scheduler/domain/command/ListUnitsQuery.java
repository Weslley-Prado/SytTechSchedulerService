package com.syttech.syttech.scheduler.scheduler.domain.command;

/** Query input for ListUnitsUseCase. */
public record ListUnitsQuery(String q, String city, int page, int size) {}
