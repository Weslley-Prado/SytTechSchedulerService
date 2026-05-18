package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.RescheduleCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;

/** Moves an Appointment to a new time window. */
public interface RescheduleAppointmentUseCase {

    Appointment rescheduleAppointment(UUID appointmentId, RescheduleCommand command);
}
