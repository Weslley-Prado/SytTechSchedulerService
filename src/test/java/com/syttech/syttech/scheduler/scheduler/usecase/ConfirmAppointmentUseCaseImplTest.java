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
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ConfirmAppointmentUseCaseImplTest {

    @Mock HoldRepositoryPort holds;
    @Mock AppointmentRepositoryPort appointments;
    @Mock CustomerRepositoryPort customers;
    @Mock PasswordHasherPort hasher;
    @Mock ApplicationEventPublisher events;

    @Test
    void confirmsWithExistingCustomer() {
        var holdId = UUID.randomUUID();
        var customerId = UUID.randomUUID();
        var hold =
                new Hold(
                        holdId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.now().plusHours(1),
                        OffsetDateTime.now().plusHours(2),
                        OffsetDateTime.now().plusMinutes(10),
                        false);
        when(holds.consume(holdId)).thenReturn(Optional.of(hold));
        when(customers.findById(customerId))
                .thenReturn(
                        Optional.of(
                                new Customer(
                                        customerId,
                                        "F",
                                        "e@x",
                                        null,
                                        "h",
                                        true,
                                        null,
                                        null,
                                        OffsetDateTime.now())));
        when(appointments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var uc = new ConfirmAppointmentUseCaseImpl(holds, appointments, customers, hasher, events);
        var result =
                uc.confirmAppointment(
                        new ConfirmAppointmentCommand(holdId, customerId, null, "key"));

        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(result.customerId()).isEqualTo(customerId);
        verify(events).publishEvent(any(Object.class));
    }

    @Test
    void createsGuestCustomerWhenNoneProvided() {
        var holdId = UUID.randomUUID();
        var hold =
                new Hold(
                        holdId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.now().plusHours(1),
                        OffsetDateTime.now().plusHours(2),
                        OffsetDateTime.now().plusMinutes(10),
                        false);
        when(holds.consume(holdId)).thenReturn(Optional.of(hold));
        when(customers.findByEmail("guest@x")).thenReturn(Optional.empty());
        when(customers.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(hasher.hash(any())).thenReturn("hashed");
        when(appointments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var uc = new ConfirmAppointmentUseCaseImpl(holds, appointments, customers, hasher, events);
        var result =
                uc.confirmAppointment(
                        new ConfirmAppointmentCommand(
                                holdId,
                                null,
                                new GuestCustomerData("Guest", "guest@x", "111", null),
                                "key"));

        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(customers).save(any());
    }

    @Test
    void rejectsExpiredHold() {
        var holdId = UUID.randomUUID();
        var hold =
                new Hold(
                        holdId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        OffsetDateTime.now().minusHours(2),
                        OffsetDateTime.now().minusHours(1),
                        OffsetDateTime.now().minusMinutes(1),
                        false);
        when(holds.consume(holdId)).thenReturn(Optional.of(hold));

        var uc = new ConfirmAppointmentUseCaseImpl(holds, appointments, customers, hasher, events);
        assertThatThrownBy(
                        () ->
                                uc.confirmAppointment(
                                        new ConfirmAppointmentCommand(
                                                holdId, UUID.randomUUID(), null, "key")))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsConsumedHold() {
        var holdId = UUID.randomUUID();
        when(holds.consume(holdId)).thenReturn(Optional.empty());

        var uc = new ConfirmAppointmentUseCaseImpl(holds, appointments, customers, hasher, events);
        assertThatThrownBy(
                        () ->
                                uc.confirmAppointment(
                                        new ConfirmAppointmentCommand(
                                                holdId, UUID.randomUUID(), null, "key")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
