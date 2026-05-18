package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;

/** Lists appointments of the authenticated customer with optional filters. */
public interface ListMyAppointmentsUseCase {

    List<Appointment> listMyAppointments(
            UUID customerId, AppointmentStatus status, OffsetDateTime from, OffsetDateTime to);
}
