package com.syttech.syttech.scheduler.scheduler.ports.in;

import com.syttech.syttech.scheduler.scheduler.domain.command.ConfirmAppointmentCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;

/** Converts a Hold into a confirmed Appointment and raises an event. */
public interface ConfirmAppointmentUseCase {

    Appointment confirmAppointment(ConfirmAppointmentCommand command);
}
