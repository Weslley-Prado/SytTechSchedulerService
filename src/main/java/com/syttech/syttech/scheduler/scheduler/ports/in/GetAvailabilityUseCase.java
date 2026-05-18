package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.List;

import com.syttech.syttech.scheduler.scheduler.domain.command.AvailabilityQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.AvailabilitySlot;

/** Returns bookable slots for the requested window. */
public interface GetAvailabilityUseCase {

    List<AvailabilitySlot> getAvailability(AvailabilityQuery query);
}
