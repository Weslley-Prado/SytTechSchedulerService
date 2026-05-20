package com.syttech.syttech.scheduler.scheduler.usecase;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.event.AppointmentCancelledEvent;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;
import com.syttech.syttech.scheduler.scheduler.ports.in.CancelAppointmentUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CancelAppointmentUseCaseImpl implements CancelAppointmentUseCase {

    private final AppointmentRepositoryPort appointments;
    private final ApplicationEventPublisher events;

    public CancelAppointmentUseCaseImpl(
            final AppointmentRepositoryPort appointments, final ApplicationEventPublisher events) {
        this.appointments = appointments;
        this.events = events;
    }

    @Override
    public void cancelAppointment(final UUID appointmentId) {
        Appointment current =
                appointments
                        .findById(appointmentId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Appointment not found: " + appointmentId));
        if (current.status() != AppointmentStatus.CONFIRMED) {
            throw new DomainValidationException("Only CONFIRMED appointments can be cancelled");
        }
        var now = OffsetDateTime.now();
        var updated =
                new Appointment(
                        current.id(),
                        current.unitId(),
                        current.serviceId(),
                        current.professionalId(),
                        current.customerId(),
                        current.code(),
                        AppointmentStatus.CANCELLED,
                        current.start(),
                        current.end(),
                        current.createdAt(),
                        now);
        appointments.save(updated);
        events.publishEvent(new AppointmentCancelledEvent(updated.id(), updated.customerId(), now));
    }
}
