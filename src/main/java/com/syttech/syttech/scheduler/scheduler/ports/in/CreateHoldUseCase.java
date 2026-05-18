package com.syttech.syttech.scheduler.scheduler.ports.in;

import com.syttech.syttech.scheduler.scheduler.domain.command.CreateHoldCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;

/** Creates a temporary pre-reservation (Hold) with TTL. */
public interface CreateHoldUseCase {

    Hold createHold(CreateHoldCommand command);
}
