package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.ListUnitsQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.Category;
import com.syttech.syttech.scheduler.scheduler.domain.model.PageResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Professional;
import com.syttech.syttech.scheduler.scheduler.domain.model.Service;
import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;

/** Read-side port for the public catalog of units/categories/services/professionals. */
public interface UnitCatalogPort {

    PageResult<Unit> findUnits(ListUnitsQuery query);

    Optional<Unit> findUnitById(UUID unitId);

    Optional<Service> findServiceById(UUID serviceId);

    List<Category> findCategoriesByUnit(UUID unitId);

    List<Service> findServicesByCategory(UUID unitId, UUID categoryId);

    List<Professional> findProfessionalsByService(UUID unitId, UUID serviceId);
}
