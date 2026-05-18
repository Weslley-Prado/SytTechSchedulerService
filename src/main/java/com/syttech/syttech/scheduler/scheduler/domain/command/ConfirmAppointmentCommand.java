package com.syttech.syttech.scheduler.scheduler.domain.command;

import java.util.UUID;

/**
 * Command input for ConfirmAppointmentUseCase. Provide customerId for authenticated flows OR guest
 * for anonymous flow.
 */
public record ConfirmAppointmentCommand(
        UUID holdId, UUID customerId, GuestCustomerData guest, String idempotencyKey) {}
