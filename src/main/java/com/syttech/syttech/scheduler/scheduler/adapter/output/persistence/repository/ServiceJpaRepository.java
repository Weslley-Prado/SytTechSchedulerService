package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.ServiceEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceJpaRepository extends JpaRepository<ServiceEntity, UUID> {

    @Query(
            "SELECT s FROM ServiceEntity s JOIN CategoryEntity c ON c.id = s.categoryId "
                    + "WHERE s.active = true AND c.unitId = :unitId AND c.id = :categoryId "
                    + "ORDER BY s.name")
    List<ServiceEntity> findByUnitAndCategory(
            @Param("unitId") UUID unitId, @Param("categoryId") UUID categoryId);
}
