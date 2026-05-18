package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.UUID;

/** Releases an active Hold. */
public interface ReleaseHoldUseCase {

    void releaseHold(UUID holdId);
}
