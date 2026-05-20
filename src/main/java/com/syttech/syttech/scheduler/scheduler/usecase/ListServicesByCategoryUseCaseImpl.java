package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Service;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListServicesByCategoryUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;

import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
@Transactional(readOnly = true)
public class ListServicesByCategoryUseCaseImpl implements ListServicesByCategoryUseCase {

    private final UnitCatalogPort catalog;

    public ListServicesByCategoryUseCaseImpl(final UnitCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Service> listServicesByCategory(final UUID unitId, final UUID categoryId) {
        return catalog.findServicesByCategory(unitId, categoryId);
    }
}
