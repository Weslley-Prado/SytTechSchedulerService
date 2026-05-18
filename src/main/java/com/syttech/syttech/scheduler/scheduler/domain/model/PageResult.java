package com.syttech.syttech.scheduler.scheduler.domain.model;

import java.util.List;

/** Pagination envelope used by the domain layer (Spring-free). */
public record PageResult<T>(
        List<T> content, int page, int size, long totalElements, int totalPages) {}
