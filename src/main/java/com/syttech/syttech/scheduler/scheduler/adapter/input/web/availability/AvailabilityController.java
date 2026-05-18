package com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability.api.AvailabilityApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.availability.dto.AvailabilityResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub controller for AvailabilityApi. Replace each method body with a call to the corresponding
 * use case (ports.in) as soon as it is implemented.
 */
@RestController
public class AvailabilityController implements AvailabilityApi {

    @Override
    public ResponseEntity<AvailabilityResponse> getAvailability(
            final UUID unitId,
            final UUID serviceId,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final UUID professionalId,
            final String timezone) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("getAvailability not implemented yet");
    }
}
