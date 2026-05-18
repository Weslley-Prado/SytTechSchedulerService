package com.syttech.syttech.scheduler.scheduler.domain.command;

/** Anonymous customer data inlined into ConfirmAppointmentCommand. */
public record GuestCustomerData(String fullName, String email, String phone, String notes) {}
