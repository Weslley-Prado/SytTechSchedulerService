package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.UUID;

/** Re-sends the appointment code e-mail (rate-limited downstream). */
public interface ResendAppointmentCodeUseCase {

    void resendAppointmentCode(UUID appointmentId);
}
