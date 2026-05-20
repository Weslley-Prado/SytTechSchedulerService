package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResendAppointmentCodeUseCaseImplTest {

    @Mock AppointmentRepositoryPort appointments;
    @Mock EmailNotificationPort emails;

    @Test
    void resendsWhenAppointmentExists() {
        var id = UUID.randomUUID();
        when(appointments.findById(id))
                .thenReturn(
                        Optional.of(
                                new Appointment(
                                        id,
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        "C",
                                        AppointmentStatus.CONFIRMED,
                                        OffsetDateTime.now(),
                                        OffsetDateTime.now().plusHours(1),
                                        OffsetDateTime.now(),
                                        OffsetDateTime.now())));

        new ResendAppointmentCodeUseCaseImpl(appointments, emails).resendAppointmentCode(id);

        verify(emails).sendAppointmentCode(id);
    }

    @Test
    void throws404WhenMissing() {
        var id = UUID.randomUUID();
        when(appointments.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new ResendAppointmentCodeUseCaseImpl(appointments, emails)
                                        .resendAppointmentCode(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(emails, never()).sendAppointmentCode(any());
    }
}
