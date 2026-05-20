package com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.api.HoldsApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.CreateHoldRequest;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.appointments.dto.HoldResponse;
import com.syttech.syttech.scheduler.scheduler.ports.in.CreateHoldUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.ReleaseHoldUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HoldsController implements HoldsApi {

    private final CreateHoldUseCase createHold;
    private final ReleaseHoldUseCase releaseHold;

    public HoldsController(
            final CreateHoldUseCase createHold, final ReleaseHoldUseCase releaseHold) {
        this.createHold = createHold;
        this.releaseHold = releaseHold;
    }

    @Override
    public ResponseEntity<HoldResponse> createHold(final CreateHoldRequest createHoldRequest) {
        var hold = createHold.createHold(AppointmentsMapper.toCommand(createHoldRequest));
        return ResponseEntity.status(201).body(AppointmentsMapper.toResponse(hold));
    }

    @Override
    public ResponseEntity<Void> releaseHold(final UUID holdId) {
        releaseHold.releaseHold(holdId);
        return ResponseEntity.noContent().build();
    }
}
