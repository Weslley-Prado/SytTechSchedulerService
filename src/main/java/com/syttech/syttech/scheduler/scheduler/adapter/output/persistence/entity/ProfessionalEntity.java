package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "professionals")
public class ProfessionalEntity {

    @Id private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(nullable = false)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private Float rating;

    @Column(nullable = false)
    private boolean active;

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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(final Float rating) {
        this.rating = rating;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}
