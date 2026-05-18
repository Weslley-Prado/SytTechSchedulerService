package com.syttech.syttech.scheduler.scheduler.ports.in;

import com.syttech.syttech.scheduler.scheduler.domain.command.ListUnitsQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.PageResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;

/** Lists active Units with optional search filters. */
public interface ListUnitsUseCase {

    PageResult<Unit> listUnits(ListUnitsQuery query);
}
