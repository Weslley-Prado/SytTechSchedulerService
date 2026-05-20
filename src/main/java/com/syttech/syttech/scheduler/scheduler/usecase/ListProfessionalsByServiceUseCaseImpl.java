package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Professional;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListProfessionalsByServiceUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListProfessionalsByServiceUseCaseImpl implements ListProfessionalsByServiceUseCase {

    private final UnitCatalogPort catalog;

    public ListProfessionalsByServiceUseCaseImpl(final UnitCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Professional> listProfessionalsByService(final UUID unitId, final UUID serviceId) {
        return catalog.findProfessionalsByService(unitId, serviceId);
    }
}
