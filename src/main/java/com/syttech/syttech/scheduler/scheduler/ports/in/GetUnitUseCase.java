package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;

/** Loads a Unit by id or fails with ResourceNotFoundException. */
public interface GetUnitUseCase {

    Unit getUnit(UUID unitId);
}
