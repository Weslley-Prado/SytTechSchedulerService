package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerUseCaseImplTest {

    @Mock CustomerRepositoryPort customers;
    @Mock PasswordHasherPort hasher;
    @Mock ApplicationEventPublisher events;

    @Test
    void registersWhenEmailNew() {
        when(customers.findByEmail("a@x")).thenReturn(Optional.empty());
        when(hasher.hash("pwd")).thenReturn("HASH");
        when(customers.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result =
                new RegisterCustomerUseCaseImpl(customers, hasher, events)
                        .registerCustomer(
                                new RegisterCustomerCommand("F", "a@x", "1", "pwd", true));

        assertThat(result.email()).isEqualTo("a@x");
        assertThat(result.passwordHash()).isEqualTo("HASH");
        assertThat(result.emailVerified()).isFalse();
        verify(events).publishEvent(any(Object.class));
    }

    @Test
    void rejectsWhenTermsNotAccepted() {
        assertThatThrownBy(
                        () ->
                                new RegisterCustomerUseCaseImpl(customers, hasher, events)
                                        .registerCustomer(
                                                new RegisterCustomerCommand(
                                                        "F", "a@x", "1", "pwd", false)))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsDuplicateEmail() {
        when(customers.findByEmail("a@x"))
                .thenReturn(
                        Optional.of(
                                new Customer(
                                        UUID.randomUUID(),
                                        "F",
                                        "a@x",
                                        null,
                                        "h",
                                        true,
                                        null,
                                        null,
                                        OffsetDateTime.now())));

        assertThatThrownBy(
                        () ->
                                new RegisterCustomerUseCaseImpl(customers, hasher, events)
                                        .registerCustomer(
                                                new RegisterCustomerCommand(
                                                        "F", "a@x", "1", "pwd", true)))
                .isInstanceOf(DomainValidationException.class);
    }
}
