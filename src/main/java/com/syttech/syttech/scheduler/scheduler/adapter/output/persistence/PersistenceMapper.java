package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence;

import java.time.DayOfWeek;
import java.util.List;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.AppointmentEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.BusinessHourEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.CategoryEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.CustomerEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.HoldEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.ProfessionalEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.ServiceEntity;
import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.UnitEntity;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.BusinessHour;
import com.syttech.syttech.scheduler.scheduler.domain.model.Category;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;
import com.syttech.syttech.scheduler.scheduler.domain.model.Professional;
import com.syttech.syttech.scheduler.scheduler.domain.model.Service;
import com.syttech.syttech.scheduler.scheduler.domain.model.Unit;

/** Entity ↔ domain converters used by the JPA adapters. */
final class PersistenceMapper {

    private PersistenceMapper() {}

    static Unit toDomain(final UnitEntity e, final List<BusinessHourEntity> hours) {
        return new Unit(
                e.getId(),
                e.getName(),
                e.getAddress(),
                e.getCity(),
                e.getPhone(),
                e.getEmail(),
                e.getCoverImageUrl(),
                hours == null
                        ? List.of()
                        : hours.stream().map(PersistenceMapper::toDomain).toList(),
                e.isActive());
    }

    static BusinessHour toDomain(final BusinessHourEntity e) {
        return new BusinessHour(
                e.getId(),
                e.getUnitId(),
                DayOfWeek.of(e.getDayOfWeek()),
                e.getOpensAt(),
                e.getClosesAt());
    }

    static Category toDomain(final CategoryEntity e) {
        return new Category(e.getId(), e.getUnitId(), e.getName(), e.getIconUrl());
    }

    static Service toDomain(final ServiceEntity e) {
        return new Service(
                e.getId(),
                e.getCategoryId(),
                e.getName(),
                e.getDescription(),
                e.getDurationMinutes(),
                e.getPrice(),
                e.getCurrency(),
                e.isActive());
    }

    static Professional toDomain(final ProfessionalEntity e) {
        return new Professional(
                e.getId(),
                e.getUnitId(),
                e.getName(),
                e.getAvatarUrl(),
                e.getRating(),
                e.isActive());
    }

    static Customer toDomain(final CustomerEntity e) {
        return new Customer(
                e.getId(),
                e.getFullName(),
                e.getEmail(),
                e.getPhone(),
                e.getPasswordHash(),
                e.isEmailVerified(),
                e.getEmailVerifyToken(),
                e.getEmailVerifyExpiresAt(),
                e.getCreatedAt());
    }

    static CustomerEntity toEntity(final Customer c, final CustomerEntity existing) {
        CustomerEntity e = existing == null ? new CustomerEntity() : existing;
        e.setId(c.id());
        e.setFullName(c.fullName());
        e.setEmail(c.email());
        e.setPhone(c.phone());
        e.setPasswordHash(c.passwordHash());
        e.setEmailVerified(c.emailVerified());
        e.setEmailVerifyToken(c.emailVerifyToken());
        e.setEmailVerifyExpiresAt(c.emailVerifyExpiresAt());
        if (e.getCreatedAt() == null) {
            e.setCreatedAt(c.createdAt() == null ? java.time.OffsetDateTime.now() : c.createdAt());
        }
        e.setUpdatedAt(java.time.OffsetDateTime.now());
        return e;
    }

    static Hold toDomain(final HoldEntity e) {
        return new Hold(
                e.getId(),
                e.getUnitId(),
                e.getServiceId(),
                e.getProfessionalId(),
                e.getStartAt(),
                e.getEndAt(),
                e.getExpiresAt(),
                e.isConsumed());
    }

    static HoldEntity toEntity(final Hold h, final HoldEntity existing) {
        HoldEntity e = existing == null ? new HoldEntity() : existing;
        e.setId(h.id());
        e.setUnitId(h.unitId());
        e.setServiceId(h.serviceId());
        e.setProfessionalId(h.professionalId());
        e.setStartAt(h.start());
        e.setEndAt(h.end());
        e.setExpiresAt(h.expiresAt());
        e.setConsumed(h.consumed());
        if (e.getCreatedAt() == null) {
            e.setCreatedAt(java.time.OffsetDateTime.now());
        }
        return e;
    }

    static Appointment toDomain(final AppointmentEntity e) {
        return new Appointment(
                e.getId(),
                e.getUnitId(),
                e.getServiceId(),
                e.getProfessionalId(),
                e.getCustomerId(),
                e.getCode(),
                e.getStatus(),
                e.getStartAt(),
                e.getEndAt(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    static AppointmentEntity toEntity(final Appointment a, final AppointmentEntity existing) {
        AppointmentEntity e = existing == null ? new AppointmentEntity() : existing;
        e.setId(a.id());
        e.setUnitId(a.unitId());
        e.setServiceId(a.serviceId());
        e.setProfessionalId(a.professionalId());
        e.setCustomerId(a.customerId());
        e.setCode(a.code());
        e.setStatus(a.status());
        e.setStartAt(a.start());
        e.setEndAt(a.end());
        if (e.getCreatedAt() == null) {
            e.setCreatedAt(a.createdAt() == null ? java.time.OffsetDateTime.now() : a.createdAt());
        }
        e.setUpdatedAt(java.time.OffsetDateTime.now());
        if (a.status()
                        == com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus
                                .CANCELLED
                && e.getCancelledAt() == null) {
            e.setCancelledAt(java.time.OffsetDateTime.now());
        }
        return e;
    }
}
