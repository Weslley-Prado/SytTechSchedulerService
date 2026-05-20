package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
class RescheduleAppointmentUseCaseImplTest {

    @Mock AppointmentRepositoryPort appointments;
    @Mock HoldRepositoryPort holds;
    @Mock UnitCatalogPort catalog;
    @Mock ApplicationEventPublisher events;

    private Appointment baseAppointment(UUID id) {
        return new Appointment(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "C",
                AppointmentStatus.CONFIRMED,
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().plusHours(2),
                OffsetDateTime.now(),
                OffsetDateTime.now());
    }

    @Test
    void reschedulesWithHold() {
        var id = UUID.randomUUID();
        var holdId = UUID.randomUUID();
        var apt = baseAppointment(id);
        when(appointments.findById(id)).thenReturn(Optional.of(apt));
        when(holds.consume(holdId))
                .thenReturn(
                        Optional.of(
                                new Hold(
                                        holdId,
                                        apt.unitId(),
                                        apt.serviceId(),
                                        apt.professionalId(),
                                        OffsetDateTime.now().plusDays(1),
                                        OffsetDateTime.now().plusDays(1).plusHours(1),
                                        OffsetDateTime.now().plusMinutes(10),
                                        false)));
        when(appointments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        new RescheduleAppointmentUseCaseImpl(appointments, holds, catalog, events)
                .rescheduleAppointment(id, new RescheduleCommand(holdId, null));

        verify(events).publishEvent(any(Object.class));
    }

    @Test
    void reschedulesWithExplicitStart() {
        var id = UUID.randomUUID();
        var apt = baseAppointment(id);
        var newStart = OffsetDateTime.now().plusDays(2);
        when(appointments.findById(id)).thenReturn(Optional.of(apt));
        when(catalog.findServiceById(apt.serviceId()))
                .thenReturn(
                        Optional.of(
                                new Service(
                                        apt.serviceId(),
                                        UUID.randomUUID(),
                                        "Cut",
                                        null,
                                        30,
                                        BigDecimal.TEN,
                                        "BRL",
                                        true)));
        when(appointments.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result =
                new RescheduleAppointmentUseCaseImpl(appointments, holds, catalog, events)
                        .rescheduleAppointment(id, new RescheduleCommand(null, newStart));

        assertThat(result.start()).isEqualTo(newStart);
        assertThat(result.end()).isEqualTo(newStart.plusMinutes(30));
    }

    @Test
    void rejectsWhenNeitherHoldNorStart() {
        var id = UUID.randomUUID();
        var apt = baseAppointment(id);
        when(appointments.findById(id)).thenReturn(Optional.of(apt));

        assertThatThrownBy(
                        () ->
                                new RescheduleAppointmentUseCaseImpl(
                                                appointments, holds, catalog, events)
                                        .rescheduleAppointment(
                                                id, new RescheduleCommand(null, null)))
                .isInstanceOf(DomainValidationException.class);
    }
}
