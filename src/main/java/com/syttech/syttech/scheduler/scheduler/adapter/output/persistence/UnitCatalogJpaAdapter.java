package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.BusinessHourJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.CategoryJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.ProfessionalJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.ServiceJpaRepository;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.UnitJpaRepository;
import com.syttech.syttech.scheduler.scheduler.domain.command.ListUnitsQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.Category;
import com.syttech.syttech.scheduler.scheduler.domain.model.PageResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Professional;
import com.syttech.syttech.scheduler.scheduler.domain.model.Service;
import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class UnitCatalogJpaAdapter implements UnitCatalogPort {

    private final UnitJpaRepository units;
    private final BusinessHourJpaRepository hours;
    private final CategoryJpaRepository categories;
    private final ServiceJpaRepository services;
    private final ProfessionalJpaRepository professionals;

    public UnitCatalogJpaAdapter(
            final UnitJpaRepository units,
            final BusinessHourJpaRepository hours,
            final CategoryJpaRepository categories,
            final ServiceJpaRepository services,
            final ProfessionalJpaRepository professionals) {
        this.units = units;
        this.hours = hours;
        this.categories = categories;
        this.services = services;
        this.professionals = professionals;
    }

    @Override
    public PageResult<Unit> findUnits(final ListUnitsQuery query) {
        int page = Math.max(query.page(), 0);
        int size = Math.max(query.size(), 1);
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var result = units.search(query.q(), query.city(), pageable);
        List<Unit> content =
                result.getContent().stream()
                        .map(u -> PersistenceMapper.toDomain(u, List.of()))
                        .toList();
        return new PageResult<>(
                content, page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public Optional<Unit> findUnitById(final UUID unitId) {
        return units.findWithHoursById(unitId)
                .map(
                        u ->
                                PersistenceMapper.toDomain(
                                        u, hours.findByUnitIdOrderByDayOfWeek(unitId)));
    }

    @Override
    public Optional<Service> findServiceById(final UUID serviceId) {
        return services.findById(serviceId).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Category> findCategoriesByUnit(final UUID unitId) {
        return categories.findByUnitIdOrderByName(unitId).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Service> findServicesByCategory(final UUID unitId, final UUID categoryId) {
        return services.findByUnitAndCategory(unitId, categoryId).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Professional> findProfessionalsByService(final UUID unitId, final UUID serviceId) {
        return professionals.findByUnitAndService(unitId, serviceId).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}
