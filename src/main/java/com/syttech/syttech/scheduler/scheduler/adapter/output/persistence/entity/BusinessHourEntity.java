package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity;

import java.time.LocalTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "business_hours")
public class BusinessHourEntity {

    @Id private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(name = "day_of_week", nullable = false)
    private short dayOfWeek;

    @Column(name = "opens_at", nullable = false)
    private LocalTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private LocalTime closesAt;

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

    public short getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(final short dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpensAt() {
        return opensAt;
    }

    public void setOpensAt(final LocalTime opensAt) {
        this.opensAt = opensAt;
    }

    public LocalTime getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(final LocalTime closesAt) {
        this.closesAt = closesAt;
    }
}
