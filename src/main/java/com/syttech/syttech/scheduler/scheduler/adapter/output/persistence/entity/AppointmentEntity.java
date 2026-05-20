package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {

    @Id private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppointmentStatus status;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

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

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(final UUID customerId) {
        this.customerId = customerId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(final AppointmentStatus status) {
        this.status = status;
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

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(final OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
