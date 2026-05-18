package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Service;

/** Lists services belonging to a category at a given Unit. */
public interface ListServicesByCategoryUseCase {

    List<Service> listServicesByCategory(UUID unitId, UUID categoryId);
}
