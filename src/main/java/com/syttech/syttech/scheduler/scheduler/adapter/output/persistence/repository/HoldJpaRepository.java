package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.HoldEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoldJpaRepository extends JpaRepository<HoldEntity, UUID> {

    @Modifying
    @Query("UPDATE HoldEntity h SET h.consumed = true " + "WHERE h.id = :id AND h.consumed = false")
    int markConsumed(@Param("id") UUID id);

    @Modifying
    @Query(
            "UPDATE HoldEntity h SET h.consumed = true "
                    + "WHERE h.consumed = false AND h.expiresAt < :now")
    int releaseExpired(@Param("now") OffsetDateTime now);

    @Query(
            "SELECT h FROM HoldEntity h WHERE h.consumed = false "
                    + "AND h.professionalId = :professionalId "
                    + "AND h.startAt < :to AND h.endAt > :from "
                    + "AND h.expiresAt > :now")
    List<HoldEntity> findActiveOverlapping(
            @Param("professionalId") UUID professionalId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("now") OffsetDateTime now);
}
