package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Category;

/** Lists categories offered by a Unit. */
public interface ListCategoriesByUnitUseCase {

    List<Category> listCategoriesByUnit(UUID unitId);
}
