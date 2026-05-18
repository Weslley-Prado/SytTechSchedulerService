package com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.api.HoldsApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.CreateHoldRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.HoldResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub controller for HoldsApi. Replace each method body with a call to the corresponding use case
 * (ports.in) as soon as it is implemented.
 */
@RestController
public class HoldsController implements HoldsApi {

    @Override
    public ResponseEntity<HoldResponse> createHold(final CreateHoldRequest createHoldRequest) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("createHold not implemented yet");
    }

    @Override
    public ResponseEntity<Void> releaseHold(final UUID holdId) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("releaseHold not implemented yet");
    }
}
