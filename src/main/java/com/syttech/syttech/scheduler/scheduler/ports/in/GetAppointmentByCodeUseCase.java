package com.syttech.syttech.scheduler.scheduler.ports.in;

import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;

/** Public lookup of an Appointment by its short code. */
public interface GetAppointmentByCodeUseCase {

    Appointment getAppointmentByCode(String code);
}
