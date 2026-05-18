package com.syttech.syttech.scheduler.scheduler.adapter.input.web.units;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.api.UnitsApi;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Category;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Professional;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.Service;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.UnitDetails;
import com.syttech.syttech.scheduler.scheduler.adapter.input.web.units.dto.UnitSummaryPage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub controller for UnitsApi. Replace each method body with a call to the corresponding use case
 * (ports.in) as soon as it is implemented.
 */
@RestController
public class UnitsController implements UnitsApi {

    @Override
    public ResponseEntity<UnitDetails> getUnit(final UUID unitId) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("getUnit not implemented yet");
    }

    @Override
    public ResponseEntity<List<Category>> listCategoriesByUnit(final UUID unitId) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("listCategoriesByUnit not implemented yet");
    }

    @Override
    public ResponseEntity<List<Professional>> listProfessionalsByService(
            final UUID unitId, final UUID serviceId) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("listProfessionalsByService not implemented yet");
    }

    @Override
    public ResponseEntity<List<Service>> listServicesByCategory(
            final UUID unitId, final UUID categoryId) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("listServicesByCategory not implemented yet");
    }

    @Override
    public ResponseEntity<UnitSummaryPage> listUnits(
            final String q, final String city, final Integer page, final Integer size) {
        // TODO: delegate to the matching use case (ports.in).
        throw new UnsupportedOperationException("listUnits not implemented yet");
    }
}
