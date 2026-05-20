package com.syttech.syttech.scheduler.scheduler.usecase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListMyAppointmentsUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListMyAppointmentsUseCaseImpl implements ListMyAppointmentsUseCase {

    private final AppointmentRepositoryPort appointments;

    public ListMyAppointmentsUseCaseImpl(final AppointmentRepositoryPort appointments) {
        this.appointments = appointments;
    }

    @Override
    public List<Appointment> listMyAppointments(
            final UUID customerId,
            final AppointmentStatus status,
            final OffsetDateTime from,
            final OffsetDateTime to) {
        return appointments.findByCustomer(customerId, status, from, to);
    }
}
