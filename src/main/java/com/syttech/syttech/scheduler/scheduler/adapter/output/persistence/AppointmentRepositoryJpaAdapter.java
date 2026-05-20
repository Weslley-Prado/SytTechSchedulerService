package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.AppointmentJpaRepository;
import com.syttech.syttech.scheduler.scheduler.domain.model.Appointment;
import com.syttech.syttech.scheduler.scheduler.domain.model.AppointmentStatus;
import com.syttech.syttech.scheduler.scheduler.ports.out.AppointmentRepositoryPort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AppointmentRepositoryJpaAdapter implements AppointmentRepositoryPort {

    private final AppointmentJpaRepository repo;

    public AppointmentRepositoryJpaAdapter(final AppointmentJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Appointment save(final Appointment appointment) {
        var existing = repo.findById(appointment.id()).orElse(null);
        var entity = PersistenceMapper.toEntity(appointment, existing);
        return PersistenceMapper.toDomain(repo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(final UUID appointmentId) {
        return repo.findById(appointmentId).map(PersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findByCode(final String code) {
        return repo.findByCode(code).map(PersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByCustomer(
            final UUID customerId,
            final AppointmentStatus status,
            final OffsetDateTime from,
            final OffsetDateTime to) {
        return repo.findByCustomer(customerId, status, from, to).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}
