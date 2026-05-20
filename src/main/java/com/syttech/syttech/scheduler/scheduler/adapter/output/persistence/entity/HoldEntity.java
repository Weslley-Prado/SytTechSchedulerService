package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "holds")
public class HoldEntity {

    @Id private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean consumed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getUnitId() {
        return unitId;
    }

    public void setUnitId(final UUID unitId) {
        this.unitId = unitId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(final UUID serviceId) {
        this.serviceId = serviceId;
    }

    public UUID getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(final UUID professionalId) {
        this.professionalId = professionalId;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(final OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(final OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(final boolean consumed) {
        this.consumed = consumed;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
