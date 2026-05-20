package com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers;

import java.time.OffsetDateTime;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.api.CustomersApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.AppointmentSummary;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.CustomerResponse;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.RegisterCustomerRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto.VerifyEmailRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.security.CurrentCustomer;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListMyAppointmentsUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.RegisterCustomerUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.VerifyCustomerEmailUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
public class CustomersController implements CustomersApi {

    private final RegisterCustomerUseCase registerCustomer;
    private final VerifyCustomerEmailUseCase verifyEmail;
    private final ListMyAppointmentsUseCase listMyAppointments;

    public CustomersController(
            final RegisterCustomerUseCase registerCustomer,
            final VerifyCustomerEmailUseCase verifyEmail,
            final ListMyAppointmentsUseCase listMyAppointments) {
        this.registerCustomer = registerCustomer;
        this.verifyEmail = verifyEmail;
        this.listMyAppointments = listMyAppointments;
    }

    @Override
    public ResponseEntity<CustomerResponse> registerCustomer(final RegisterCustomerRequest req) {
        var created = registerCustomer.registerCustomer(CustomersMapper.toCommand(req));
        return ResponseEntity.status(201).body(CustomersMapper.toResponse(created));
    }

    @Override
    public ResponseEntity<Void> verifyCustomerEmail(final VerifyEmailRequest req) {
        verifyEmail.verifyCustomerEmail(CustomersMapper.tokenOf(req));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<AppointmentSummary>> listMyAppointments(
            final com.syttech.syttech.scheduler.scheduler.adapter.input.web.customers.dto
                            .AppointmentStatus
                    status,
            final OffsetDateTime from,
            final OffsetDateTime to) {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();
        var customerId = CurrentCustomer.requireId(request);
        AppointmentStatus domainStatus =
                status == null ? null : AppointmentStatus.valueOf(status.name());
        return ResponseEntity.ok(
                CustomersMapper.toSummaries(
                        listMyAppointments.listMyAppointments(customerId, domainStatus, from, to)));
    }
}
