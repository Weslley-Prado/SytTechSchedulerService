package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;

/** Persistence port for the Hold aggregate. */
public interface HoldRepositoryPort {

    Hold save(Hold hold);

    Optional<Hold> findById(UUID holdId);

    void deleteById(UUID holdId);

    /** Atomically marks the hold as consumed (used by ConfirmAppointment). */
    Optional<Hold> consume(UUID holdId);

    /** Releases all holds whose expiresAt is in the past. Used by a scheduled job. */
    int releaseExpired();
}
