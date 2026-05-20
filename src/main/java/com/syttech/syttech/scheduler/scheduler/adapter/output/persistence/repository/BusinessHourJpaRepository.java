package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.BusinessHourEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessHourJpaRepository extends JpaRepository<BusinessHourEntity, UUID> {
    List<BusinessHourEntity> findByUnitIdOrderByDayOfWeek(UUID unitId);
}
