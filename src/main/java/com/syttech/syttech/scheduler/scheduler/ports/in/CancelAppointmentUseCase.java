package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.UUID;

/** Cancels a confirmed Appointment. */
public interface CancelAppointmentUseCase {

    void cancelAppointment(UUID appointmentId);
}
