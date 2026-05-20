package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.CategoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByUnitIdOrderByName(UUID unitId);
}
