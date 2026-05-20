package com.syttech.syttech.scheduler.scheduler.adapter.input.web.units;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.api.UnitsApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Category;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Professional;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Service;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.UnitDetails;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.UnitSummaryPage;
import com.syttech.syttech.scheduler.scheduler.domain.command.ListUnitsQuery;
import com.syttech.syttech.scheduler.scheduler.ports.in.GetUnitUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListCategoriesByUnitUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListProfessionalsByServiceUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListServicesByCategoryUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.in.ListUnitsUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnitsController implements UnitsApi {

    private final ListUnitsUseCase listUnits;
    private final GetUnitUseCase getUnit;
    private final ListCategoriesByUnitUseCase listCategories;
    private final ListServicesByCategoryUseCase listServices;
    private final ListProfessionalsByServiceUseCase listProfessionals;

    public UnitsController(
            final ListUnitsUseCase listUnits,
            final GetUnitUseCase getUnit,
            final ListCategoriesByUnitUseCase listCategories,
            final ListServicesByCategoryUseCase listServices,
            final ListProfessionalsByServiceUseCase listProfessionals) {
        this.listUnits = listUnits;
        this.getUnit = getUnit;
        this.listCategories = listCategories;
        this.listServices = listServices;
        this.listProfessionals = listProfessionals;
    }

    @Override
    public ResponseEntity<UnitSummaryPage> listUnits(
            final String q, final String city, final Integer page, final Integer size) {
        var result =
                listUnits.listUnits(
                        new ListUnitsQuery(
                                q, city, page == null ? 0 : page, size == null ? 20 : size));
        return ResponseEntity.ok(UnitsMapper.toPage(result));
    }

    @Override
    public ResponseEntity<UnitDetails> getUnit(final UUID unitId) {
        return ResponseEntity.ok(UnitsMapper.toDetails(getUnit.getUnit(unitId)));
    }

    @Override
    public ResponseEntity<List<Category>> listCategoriesByUnit(final UUID unitId) {
        return ResponseEntity.ok(
                listCategories.listCategoriesByUnit(unitId).stream()
                        .map(UnitsMapper::toCategory)
                        .toList());
    }

    @Override
    public ResponseEntity<List<Service>> listServicesByCategory(
            final UUID unitId, final UUID categoryId) {
        return ResponseEntity.ok(
                listServices.listServicesByCategory(unitId, categoryId).stream()
                        .map(UnitsMapper::toService)
                        .toList());
    }

    @Override
    public ResponseEntity<List<Professional>> listProfessionalsByService(
            final UUID unitId, final UUID serviceId) {
        return ResponseEntity.ok(
                listProfessionals.listProfessionalsByService(unitId, serviceId).stream()
                        .map(UnitsMapper::toProfessional)
                        .toList());
    }
}
