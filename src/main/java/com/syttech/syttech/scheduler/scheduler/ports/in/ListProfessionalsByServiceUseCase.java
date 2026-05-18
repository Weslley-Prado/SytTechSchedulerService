package com.syttech.syttech.scheduler.scheduler.ports.in;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Professional;

/** Lists professionals able to execute the service at the Unit. */
public interface ListProfessionalsByServiceUseCase {

    List<Professional> listProfessionalsByService(UUID unitId, UUID serviceId);
}
