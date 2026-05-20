package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.AppointmentEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.HoldEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.ServiceEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.AppointmentJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.BusinessHourJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.HoldJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.ProfessionalJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.ServiceJpaRepository;
import com.syttech.syttech.scheduler.scheduler.domain.command.AvailabilityQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.AvailabilitySlot;
import com.syttech.syttech.scheduler.scheduler.ports.out.AvailabilityQueryPort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Naive availability computation: for each professional and each day in [from..to], expands
 * candidate slots from business hours and discards those overlapping with active holds or confirmed
 * appointments. Slot granularity = service duration.
 */
@Component
@Transactional(readOnly = true)
public class AvailabilityQueryJpaAdapter implements AvailabilityQueryPort {

    private final ServiceJpaRepository services;
    private final ProfessionalJpaRepository professionals;
    private final BusinessHourJpaRepository hours;
    private final HoldJpaRepository holds;
    private final AppointmentJpaRepository appointments;

    public AvailabilityQueryJpaAdapter(
            final ServiceJpaRepository services,
            final ProfessionalJpaRepository professionals,
            final BusinessHourJpaRepository hours,
            final HoldJpaRepository holds,
            final AppointmentJpaRepository appointments) {
        this.services = services;
        this.professionals = professionals;
        this.hours = hours;
        this.holds = holds;
        this.appointments = appointments;
    }

    @Override
    public List<AvailabilitySlot> findAvailableSlots(final AvailabilityQuery query) {
        ServiceEntity service = services.findById(query.serviceId()).orElse(null);
        if (service == null) {
            return List.of();
        }
        int duration = service.getDurationMinutes();
        ZoneId zone =
                query.timezone() == null || query.timezone().isBlank()
                        ? ZoneOffset.UTC
                        : ZoneId.of(query.timezone());

        List<UUID> profIds;
        if (query.professionalId() != null) {
            profIds = List.of(query.professionalId());
        } else {
            profIds =
                    professionals.findByUnitAndService(query.unitId(), query.serviceId()).stream()
                            .map(p -> p.getId())
                            .toList();
        }

        var bhByDow = new java.util.HashMap<Integer, List<LocalTime[]>>();
        hours.findByUnitIdOrderByDayOfWeek(query.unitId())
                .forEach(
                        h ->
                                bhByDow.computeIfAbsent(
                                                (int) h.getDayOfWeek(), k -> new ArrayList<>())
                                        .add(new LocalTime[] {h.getOpensAt(), h.getClosesAt()}));

        OffsetDateTime now = OffsetDateTime.now();
        List<AvailabilitySlot> out = new ArrayList<>();

        for (UUID profId : profIds) {
            List<HoldEntity> activeHolds =
                    holds.findActiveOverlapping(profId, query.from(), query.to(), now);
            List<AppointmentEntity> activeAppts =
                    appointments.findActiveOverlapping(profId, query.from(), query.to());

            LocalDate day = query.from().atZoneSameInstant(zone).toLocalDate();
            LocalDate endDay = query.to().atZoneSameInstant(zone).toLocalDate();
            while (!day.isAfter(endDay)) {
                int dow = day.getDayOfWeek().getValue();
                List<LocalTime[]> windows = bhByDow.getOrDefault(dow, List.of());
                for (LocalTime[] w : windows) {
                    OffsetDateTime cursor =
                            OffsetDateTime.of(
                                    day, w[0], zone.getRules().getOffset(day.atTime(w[0])));
                    OffsetDateTime closesAt =
                            OffsetDateTime.of(
                                    day, w[1], zone.getRules().getOffset(day.atTime(w[1])));
                    while (!cursor.plusMinutes(duration).isAfter(closesAt)) {
                        OffsetDateTime slotEnd = cursor.plusMinutes(duration);
                        if (!cursor.isBefore(query.from())
                                && !slotEnd.isAfter(query.to())
                                && noConflict(cursor, slotEnd, activeHolds, activeAppts)) {
                            out.add(new AvailabilitySlot(cursor, slotEnd, profId));
                        }
                        cursor = cursor.plusMinutes(duration);
                    }
                }
                day = day.plusDays(1);
            }
        }
        return out;
    }

    private static boolean noConflict(
            final OffsetDateTime start,
            final OffsetDateTime end,
            final List<HoldEntity> activeHolds,
            final List<AppointmentEntity> activeAppts) {
        for (HoldEntity h : activeHolds) {
            if (start.isBefore(h.getEndAt()) && end.isAfter(h.getStartAt())) {
                return false;
            }
        }
        for (AppointmentEntity a : activeAppts) {
            if (start.isBefore(a.getEndAt()) && end.isAfter(a.getStartAt())) {
                return false;
            }
        }
        return true;
    }
}
