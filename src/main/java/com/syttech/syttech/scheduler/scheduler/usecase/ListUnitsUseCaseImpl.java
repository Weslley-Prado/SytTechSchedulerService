package com.syttech.syttech.scheduler.scheduler.usecase;

import com.syttech.syttech.scheduler.scheduler.domain.command.ListUnitsQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.PageResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListUnitsUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListUnitsUseCaseImpl implements ListUnitsUseCase {

    private final UnitCatalogPort catalog;

    public ListUnitsUseCaseImpl(final UnitCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public PageResult<Unit> listUnits(final ListUnitsQuery query) {
        return catalog.findUnits(query);
    }
}
