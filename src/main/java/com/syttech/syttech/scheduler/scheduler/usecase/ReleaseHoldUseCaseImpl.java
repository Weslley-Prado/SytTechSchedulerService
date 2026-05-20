package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.ports.in.ReleaseHoldUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.HoldRepositoryPort;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReleaseHoldUseCaseImpl implements ReleaseHoldUseCase {

    private final HoldRepositoryPort holds;

    public ReleaseHoldUseCaseImpl(final HoldRepositoryPort holds) {
        this.holds = holds;
    }

    @Override
    public void releaseHold(final UUID holdId) {
        holds.findById(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold not found: " + holdId));
        holds.deleteById(holdId);
    }
}
