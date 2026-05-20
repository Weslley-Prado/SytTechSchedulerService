package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class CategoryEntity {

    @Id private UUID id;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Column(nullable = false)
    private String name;

    @Column(name = "icon_url")
    private String iconUrl;

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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(final String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
