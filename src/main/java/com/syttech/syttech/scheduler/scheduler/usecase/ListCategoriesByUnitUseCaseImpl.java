package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Category;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListCategoriesByUnitUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListCategoriesByUnitUseCaseImpl implements ListCategoriesByUnitUseCase {

    private final UnitCatalogPort catalog;

    public ListCategoriesByUnitUseCaseImpl(final UnitCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Category> listCategoriesByUnit(final UUID unitId) {
        return catalog.findCategoriesByUnit(unitId);
    }
}
