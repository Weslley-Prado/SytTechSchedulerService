package com.syttech.syttech.scheduler.scheduler.usecase;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.RegisterCustomerCommand;
import com.syttech.syttech.scheduler.scheduler.domain.event.CustomerRegisteredEvent;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.ports.in.RegisterCustomerUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.CustomerRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.PasswordHasherPort;
import com.syttech.syttech.scheduler.scheduler.usecase.util.CodeGenerator;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterCustomerUseCaseImpl implements RegisterCustomerUseCase {

    private final CustomerRepositoryPort customers;
    private final PasswordHasherPort hasher;
    private final ApplicationEventPublisher events;

    public RegisterCustomerUseCaseImpl(
            final CustomerRepositoryPort customers,
            final PasswordHasherPort hasher,
            final ApplicationEventPublisher events) {
        this.customers = customers;
        this.hasher = hasher;
        this.events = events;
    }

    @Override
    public Customer registerCustomer(final RegisterCustomerCommand command) {
        if (!command.acceptTerms()) {
            throw new DomainValidationException("Terms must be accepted");
        }
        customers
                .findByEmail(command.email())
                .ifPresent(
                        c -> {
                            throw new DomainValidationException("E-mail already registered");
                        });
        String token = CodeGenerator.verificationToken();
        var customer =
                new Customer(
                        UUID.randomUUID(),
                        command.fullName(),
                        command.email(),
                        command.phone(),
                        hasher.hash(command.password()),
                        false,
                        token,
                        OffsetDateTime.now().plusDays(7),
                        OffsetDateTime.now());
        customer = customers.save(customer);
        events.publishEvent(new CustomerRegisteredEvent(customer.id(), customer.email(), token));
        return customer;
    }
}
