package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;

/** Persistence port for the Appointment aggregate. */
public interface AppointmentRepositoryPort {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(UUID appointmentId);

    Optional<Appointment> findByCode(String code);

    List<Appointment> findByCustomer(
            UUID customerId, AppointmentStatus status, OffsetDateTime from, OffsetDateTime to);
}
