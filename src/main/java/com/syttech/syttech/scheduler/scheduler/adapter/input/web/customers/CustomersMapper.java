package com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers;

import java.util.List;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.AppointmentSummary;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.CustomerResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.RegisterCustomerRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.VerifyEmailRequest;
import com.syttech.syttech.scheduler.scheduler.domain.command.RegisterCustomerCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;

/** DTO ↔ domain converters for the Customers API. */
final class CustomersMapper {

    private CustomersMapper() {}

    static RegisterCustomerCommand toCommand(final RegisterCustomerRequest req) {
        return new RegisterCustomerCommand(
                req.getFullName(),
                req.getEmail(),
                req.getPhone(),
                req.getPassword(),
                Boolean.TRUE.equals(req.getAcceptTerms()));
    }

    static CustomerResponse toResponse(final Customer c) {
        return new CustomerResponse()
                .id(c.id())
                .fullName(c.fullName())
                .email(c.email())
                .phone(c.phone())
                .emailVerified(c.emailVerified());
    }

    static String tokenOf(final VerifyEmailRequest req) {
        return req.getToken();
    }

    static List<AppointmentSummary> toSummaries(final List<Appointment> list) {
        return list.stream()
                .map(
                        a ->
                                new AppointmentSummary()
                                        .appointmentId(a.id())
                                        .code(a.code())
                                        .status(
                                                com.syttech.syttech.scheduler.scheduler.adapter
                                                        .input.web.customers.dto.AppointmentStatus
                                                        .valueOf(a.status().name()))
                                        .start(a.start())
                                        .end(a.end())
                                        .serviceName(null)
                                        .professionalName(null)
                                        .unitName(null))
                .toList();
    }
}
