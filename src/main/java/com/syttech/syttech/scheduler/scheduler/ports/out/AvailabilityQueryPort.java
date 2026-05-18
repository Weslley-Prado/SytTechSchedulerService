package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.util.List;

import com.syttech.syttech.scheduler.scheduler.domain.command.AvailabilityQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.AvailabilitySlot;

/** Computes free slots out of business hours, holds and existing appointments. */
public interface AvailabilityQueryPort {

    List<AvailabilitySlot> findAvailableSlots(AvailabilityQuery query);
}
