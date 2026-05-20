package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.ports.in.ResendAppointmentCodeUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.EmailNotificationPort;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ResendAppointmentCodeUseCaseImpl implements ResendAppointmentCodeUseCase {

    private final AppointmentRepositoryPort appointments;
    private final EmailNotificationPort emails;

    public ResendAppointmentCodeUseCaseImpl(
            final AppointmentRepositoryPort appointments, final EmailNotificationPort emails) {
        this.appointments = appointments;
        this.emails = emails;
    }

    @Override
    public void resendAppointmentCode(final UUID appointmentId) {
        appointments
                .findById(appointmentId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Appointment not found: " + appointmentId));
        emails.sendAppointmentCode(appointmentId);
    }
}
