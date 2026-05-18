package com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers;

import java.time.OffsetDateTime;
import java.util.List;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.api.CustomersApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.AppointmentStatus;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.AppointmentSummary;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.CustomerResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.RegisterCustomerRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.VerifyEmailRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub controller for CustomersApi. Replace each method body with a call to the corresponding use
 * case (ports.in) as soon as it is implemented.
 */
@RestController
public class CustomersController implements CustomersApi {

    @Override
    public ResponseEntity<List<AppointmentSummary>> listMyAppointments(
            final AppointmentStatus status, final OffsetDateTime from, final OffsetDateTime to) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("listMyAppointments not implemented yet");
    }

    @Override
    public ResponseEntity<CustomerResponse> registerCustomer(
            final RegisterCustomerRequest registerCustomerRequest) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("registerCustomer not implemented yet");
    }

    @Override
    public ResponseEntity<Void> verifyCustomerEmail(final VerifyEmailRequest verifyEmailRequest) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("verifyCustomerEmail not implemented yet");
    }
}
