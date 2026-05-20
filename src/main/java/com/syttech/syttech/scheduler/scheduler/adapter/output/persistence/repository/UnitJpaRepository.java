package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.UnitEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnitJpaRepository extends JpaRepository<UnitEntity, UUID> {

    @Query(
            "SELECT u FROM UnitEntity u WHERE u.active = true "
                    + "AND (CAST(:city AS string) IS NULL "
                    + "     OR LOWER(u.city) = LOWER(CAST(:city AS string))) "
                    + "AND (CAST(:q AS string) IS NULL "
                    + "     OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))")
    Page<UnitEntity> search(@Param("q") String q, @Param("city") String city, Pageable pageable);

    @EntityGraph(attributePaths = "businessHours")
    java.util.Optional<UnitEntity> findWithHoursById(UUID id);
}
