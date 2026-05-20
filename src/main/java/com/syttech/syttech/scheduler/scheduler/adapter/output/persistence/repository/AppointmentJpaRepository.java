package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.AppointmentEntity;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {

    Optional<AppointmentEntity> findByCode(String code);

    @Query(
            "SELECT a FROM AppointmentEntity a WHERE a.customerId = :customerId "
                    + "AND (:status IS NULL OR a.status = :status) "
                    + "AND (:from IS NULL OR a.startAt >= :from) "
                    + "AND (:to IS NULL OR a.startAt <= :to) "
                    + "ORDER BY a.startAt DESC")
    List<AppointmentEntity> findByCustomer(
            @Param("customerId") UUID customerId,
            @Param("status") AppointmentStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query(
            "SELECT a FROM AppointmentEntity a WHERE a.status = "
                    + "com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus.CONFIRMED "
                    + "AND a.professionalId = :professionalId "
                    + "AND a.startAt < :to AND a.endAt > :from")
    List<AppointmentEntity> findActiveOverlapping(
            @Param("professionalId") UUID professionalId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}
