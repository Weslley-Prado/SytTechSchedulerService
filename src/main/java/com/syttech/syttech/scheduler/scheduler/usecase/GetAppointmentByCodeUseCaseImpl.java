package com.syttech.syttech.scheduler.scheduler.usecase;

import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.ports.in.GetAppointmentByCodeUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetAppointmentByCodeUseCaseImpl implements GetAppointmentByCodeUseCase {

    private final AppointmentRepositoryPort appointments;

    public GetAppointmentByCodeUseCaseImpl(final AppointmentRepositoryPort appointments) {
        this.appointments = appointments;
    }

    @Override
    public Appointment getAppointmentByCode(final String code) {
        return appointments
                .findByCode(code)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Appointment not found for code: " + code));
    }
}
