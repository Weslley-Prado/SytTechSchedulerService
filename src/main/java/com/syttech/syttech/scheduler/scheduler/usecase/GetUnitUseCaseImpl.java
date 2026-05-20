package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;
import com.syttech.syttech.scheduler.scheduler.ports.in.GetUnitUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetUnitUseCaseImpl implements GetUnitUseCase {

    private final UnitCatalogPort catalog;

    public GetUnitUseCaseImpl(final UnitCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public Unit getUnit(final UUID unitId) {
        return catalog.findUnitById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
    }
}
