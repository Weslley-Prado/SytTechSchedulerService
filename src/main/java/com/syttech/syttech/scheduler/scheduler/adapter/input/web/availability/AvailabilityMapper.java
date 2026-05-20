package com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability.dto.AvailabilityResponse;
import com.syttech.syttech.scheduler.scheduler.domain.command.AvailabilityQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.AvailabilitySlot;

/** DTO ↔ domain converters for the Availability API. */
final class AvailabilityMapper {

    private AvailabilityMapper() {}

    static AvailabilityQuery toQuery(
            final UUID unitId,
            final UUID serviceId,
            final UUID professionalId,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final String timezone) {
        return new AvailabilityQuery(unitId, serviceId, professionalId, from, to, timezone);
    }

    static AvailabilityResponse toResponse(final List<AvailabilitySlot> slots) {
        AvailabilityResponse r = new AvailabilityResponse();
        slots.forEach(
                s ->
                        r.addSlotsItem(
                                new com.syttech.syttech.scheduler.scheduler.adapter.input.web
                                                .availability.dto.AvailabilitySlot()
                                        .start(s.start())
                                        .end(s.end())
                                        .professionalId(s.professionalId())));
        return r;
    }
}
