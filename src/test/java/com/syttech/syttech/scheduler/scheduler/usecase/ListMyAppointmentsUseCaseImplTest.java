package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMyAppointmentsUseCaseImplTest {

    @Mock AppointmentRepositoryPort appointments;

    @Test
    void delegatesToRepo() {
        var customerId = UUID.randomUUID();
        var now = OffsetDateTime.now();
        var list = List.<Appointment>of();
        when(appointments.findByCustomer(
                        customerId, AppointmentStatus.CONFIRMED, now, now.plusDays(7)))
                .thenReturn(list);

        assertThat(
                        new ListMyAppointmentsUseCaseImpl(appointments)
                                .listMyAppointments(
                                        customerId,
                                        AppointmentStatus.CONFIRMED,
                                        now,
                                        now.plusDays(7)))
                .isSameAs(list);
    }
}
