package com.syttech.syttech.scheduler.scheduler.usecase;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.RescheduleCommand;
import com.syttech.syttech.scheduler.scheduler.domain.event.AppointmentRescheduledEvent;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;
import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;
import com.syttech.syttech.scheduler.scheduler.ports.in.RescheduleAppointmentUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.HoldRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RescheduleAppointmentUseCaseImpl implements RescheduleAppointmentUseCase {

    private final AppointmentRepositoryPort appointments;
    private final HoldRepositoryPort holds;
    private final UnitCatalogPort catalog;
    private final ApplicationEventPublisher events;

    public RescheduleAppointmentUseCaseImpl(
            final AppointmentRepositoryPort appointments,
            final HoldRepositoryPort holds,
            final UnitCatalogPort catalog,
            final ApplicationEventPublisher events) {
        this.appointments = appointments;
        this.holds = holds;
        this.catalog = catalog;
        this.events = events;
    }

    @Override
    public Appointment rescheduleAppointment(
            final UUID appointmentId, final RescheduleCommand command) {
        Appointment current =
                appointments
                        .findById(appointmentId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Appointment not found: " + appointmentId));
        if (current.status() != AppointmentStatus.CONFIRMED) {
            throw new DomainValidationException("Only CONFIRMED appointments can be rescheduled");
        }

        OffsetDateTime newStart;
        OffsetDateTime newEnd;
        if (command.holdId() != null) {
            Hold hold =
                    holds.consume(command.holdId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Hold not found: " + command.holdId()));
            newStart = hold.start();
            newEnd = hold.end();
        } else if (command.start() != null) {
            var service =
                    catalog.findServiceById(current.serviceId())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Service not found: " + current.serviceId()));
            newStart = command.start();
            newEnd = newStart.plusMinutes(service.durationMinutes());
        } else {
            throw new DomainValidationException("Either holdId or start must be provided");
        }

        var previousStart = current.start();
        var now = OffsetDateTime.now();
        var updated =
                new Appointment(
                        current.id(),
                        current.unitId(),
                        current.serviceId(),
                        current.professionalId(),
                        current.customerId(),
                        current.code(),
                        current.status(),
                        newStart,
                        newEnd,
                        current.createdAt(),
                        now);
        updated = appointments.save(updated);
        events.publishEvent(new AppointmentRescheduledEvent(updated.id(), previousStart, newStart));
        return updated;
    }
}
