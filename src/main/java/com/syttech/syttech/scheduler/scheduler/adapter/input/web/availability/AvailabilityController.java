package com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability.api.AvailabilityApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability.dto.AvailabilityResponse;
import com.syttech.syttech.scheduler.scheduler.ports.in.GetAvailabilityUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvailabilityController implements AvailabilityApi {

    private final GetAvailabilityUseCase getAvailability;

    public AvailabilityController(final GetAvailabilityUseCase getAvailability) {
        this.getAvailability = getAvailability;
    }

    @Override
    public ResponseEntity<AvailabilityResponse> getAvailability(
            final UUID unitId,
            final UUID serviceId,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final UUID professionalId,
            final String timezone) {
        var query =
                AvailabilityMapper.toQuery(unitId, serviceId, professionalId, from, to, timezone);
        return ResponseEntity.ok(
                AvailabilityMapper.toResponse(getAvailability.getAvailability(query)));
    }
}
