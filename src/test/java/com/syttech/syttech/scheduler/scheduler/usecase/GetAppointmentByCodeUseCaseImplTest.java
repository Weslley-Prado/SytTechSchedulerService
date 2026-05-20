package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class GetAppointmentByCodeUseCaseImplTest {

    @Mock AppointmentRepositoryPort appointments;

    @Test
    void returnsByCode() {
        var apt =
                new Appointment(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "CODE-1",
                        AppointmentStatus.CONFIRMED,
                        OffsetDateTime.now(),
                        OffsetDateTime.now().plusHours(1),
                        OffsetDateTime.now(),
                        OffsetDateTime.now());
        when(appointments.findByCode("CODE-1")).thenReturn(Optional.of(apt));

        assertThat(new GetAppointmentByCodeUseCaseImpl(appointments).getAppointmentByCode("CODE-1"))
                .isSameAs(apt);
    }

    @Test
    void throws404WhenMissing() {
        when(appointments.findByCode("X")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new GetAppointmentByCodeUseCaseImpl(appointments)
                                        .getAppointmentByCode("X"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
