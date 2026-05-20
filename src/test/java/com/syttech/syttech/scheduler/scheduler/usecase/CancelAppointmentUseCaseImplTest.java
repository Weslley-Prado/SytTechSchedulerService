package com.syttech.syttech.scheduler.scheduler.usecase;

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
class CancelAppointmentUseCaseImplTest {

    @Mock AppointmentRepositoryPort appointments;
    @Mock ApplicationEventPublisher events;

    private Appointment confirmed(UUID id) {
        return new Appointment(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CODE-1234",
                AppointmentStatus.CONFIRMED,
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().plusHours(2),
                OffsetDateTime.now(),
                OffsetDateTime.now());
    }

    @Test
    void cancelsConfirmedAppointment() {
        var id = UUID.randomUUID();
        when(appointments.findById(id)).thenReturn(Optional.of(confirmed(id)));
        when(appointments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        new CancelAppointmentUseCaseImpl(appointments, events).cancelAppointment(id);

        verify(events).publishEvent(any(Object.class));
    }

    @Test
    void rejectsAlreadyCancelled() {
        var id = UUID.randomUUID();
        var existing = confirmed(id);
        var cancelled =
                new Appointment(
                        existing.id(),
                        existing.unitId(),
                        existing.serviceId(),
                        existing.professionalId(),
                        existing.customerId(),
                        existing.code(),
                        AppointmentStatus.CANCELLED,
                        existing.start(),
                        existing.end(),
                        existing.createdAt(),
                        existing.updatedAt());
        when(appointments.findById(id)).thenReturn(Optional.of(cancelled));

        assertThatThrownBy(
                        () ->
                                new CancelAppointmentUseCaseImpl(appointments, events)
                                        .cancelAppointment(id))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void throws404WhenMissing() {
        var id = UUID.randomUUID();
        when(appointments.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new CancelAppointmentUseCaseImpl(appointments, events)
                                        .cancelAppointment(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
