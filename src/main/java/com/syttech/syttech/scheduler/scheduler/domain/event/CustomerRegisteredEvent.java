package com.syttech.syttech.scheduler.scheduler.domain.event;

import java.util.UUID;

/** Raised when a Customer registers and needs e-mail verification. */
public record CustomerRegisteredEvent(UUID customerId, String email, String verifyToken) {}
