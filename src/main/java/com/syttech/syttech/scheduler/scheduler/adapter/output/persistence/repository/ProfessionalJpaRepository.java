package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.ProfessionalEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfessionalJpaRepository extends JpaRepository<ProfessionalEntity, UUID> {

    @Query(
            value =
                    "SELECT p.* FROM professionals p "
                            + "JOIN service_professionals sp ON sp.professional_id = p.id "
                            + "WHERE p.active = TRUE AND p.unit_id = :unitId "
                            + "AND sp.service_id = :serviceId ORDER BY p.name",
            nativeQuery = true)
    List<ProfessionalEntity> findByUnitAndService(
            @Param("unitId") UUID unitId, @Param("serviceId") UUID serviceId);
}
