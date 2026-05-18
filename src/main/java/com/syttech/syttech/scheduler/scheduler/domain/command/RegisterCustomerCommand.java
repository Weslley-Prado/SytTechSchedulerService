package com.syttech.syttech.scheduler.scheduler.domain.command;

/** Command input for RegisterCustomerUseCase. */
public record RegisterCustomerCommand(
        String fullName, String email, String phone, String password, boolean acceptTerms) {}
