package com.syttech.syttech.scheduler.scheduler.usecase;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.ConfirmAppointmentCommand;
import com.syttech.syttech.scheduler.scheduler.domain.command.GuestCustomerData;
import com.syttech.syttech.scheduler.scheduler.domain.event.AppointmentConfirmedEvent;
import com.syttech.syttech.scheduler.scheduler.domain.event.CustomerRegisteredEvent;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;
import com.syttech.syttech.scheduler.scheduler.ports.in.ConfirmAppointmentUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.CustomerRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.HoldRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.PasswordHasherPort;
import com.syttech.syttech.scheduler.scheduler.usecase.util.CodeGenerator;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfirmAppointmentUseCaseImpl implements ConfirmAppointmentUseCase {

    private final HoldRepositoryPort holds;
    private final AppointmentRepositoryPort appointments;
    private final CustomerRepositoryPort customers;
    private final PasswordHasherPort hasher;
    private final ApplicationEventPublisher events;

    public ConfirmAppointmentUseCaseImpl(
            final HoldRepositoryPort holds,
            final AppointmentRepositoryPort appointments,
            final CustomerRepositoryPort customers,
            final PasswordHasherPort hasher,
            final ApplicationEventPublisher events) {
        this.holds = holds;
        this.appointments = appointments;
        this.customers = customers;
        this.hasher = hasher;
        this.events = events;
    }

    @Override
    public Appointment confirmAppointment(final ConfirmAppointmentCommand command) {
        Hold hold =
                holds.consume(command.holdId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Hold not found or already consumed: "
                                                        + command.holdId()));
        if (hold.expiresAt().isBefore(OffsetDateTime.now())) {
            throw new DomainValidationException("Hold expired");
        }

        UUID customerId = resolveCustomerId(command);
        String code = CodeGenerator.shortAppointmentCode();
        var now = OffsetDateTime.now();
        var appointment =
                new Appointment(
                        UUID.randomUUID(),
                        hold.unitId(),
                        hold.serviceId(),
                        hold.professionalId(),
                        customerId,
                        code,
                        AppointmentStatus.CONFIRMED,
                        hold.start(),
                        hold.end(),
                        now,
                        now);
        appointment = appointments.save(appointment);

        events.publishEvent(
                new AppointmentConfirmedEvent(
                        appointment.id(),
                        appointment.code(),
                        customerId,
                        resolveEmail(command, customerId),
                        appointment.start()));
        return appointment;
    }

    private UUID resolveCustomerId(final ConfirmAppointmentCommand command) {
        if (command.customerId() != null) {
            return customers
                    .findById(command.customerId())
                    .map(Customer::id)
                    .orElseThrow(
                            () ->
                                    new ResourceNotFoundException(
                                            "Customer not found: " + command.customerId()));
        }
        GuestCustomerData guest = command.guest();
        if (guest == null) {
            throw new DomainValidationException("Either customerId or guest data must be provided");
        }
        return customers
                .findByEmail(guest.email())
                .map(Customer::id)
                .orElseGet(() -> createGuestAccount(guest));
    }

    private UUID createGuestAccount(final GuestCustomerData guest) {
        String token = CodeGenerator.verificationToken();
        var customer =
                new Customer(
                        UUID.randomUUID(),
                        guest.fullName(),
                        guest.email(),
                        guest.phone(),
                        hasher.hash(CodeGenerator.verificationToken()),
                        false,
                        token,
                        OffsetDateTime.now().plusDays(7),
                        OffsetDateTime.now());
        customer = customers.save(customer);
        events.publishEvent(new CustomerRegisteredEvent(customer.id(), customer.email(), token));
        return customer.id();
    }

    private String resolveEmail(final ConfirmAppointmentCommand command, final UUID customerId) {
        if (command.guest() != null) {
            return command.guest().email();
        }
        return customers.findById(customerId).map(Customer::email).orElse(null);
    }
}
