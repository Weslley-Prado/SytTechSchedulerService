package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.HoldJpaRepository;
import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;
import com.syttech.syttech.scheduler.scheduler.ports.out.HoldRepositoryPort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class HoldRepositoryJpaAdapter implements HoldRepositoryPort {

    private final HoldJpaRepository repo;

    public HoldRepositoryJpaAdapter(final HoldJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Hold save(final Hold hold) {
        var existing = repo.findById(hold.id()).orElse(null);
        var entity = PersistenceMapper.toEntity(hold, existing);
        return PersistenceMapper.toDomain(repo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hold> findById(final UUID holdId) {
        return repo.findById(holdId).map(PersistenceMapper::toDomain);
    }

    @Override
    public void deleteById(final UUID holdId) {
        repo.deleteById(holdId);
    }

    @Override
    public Optional<Hold> consume(final UUID holdId) {
        int updated = repo.markConsumed(holdId);
        if (updated == 0) {
            return Optional.empty();
        }
        return repo.findById(holdId).map(PersistenceMapper::toDomain);
    }

    @Override
    public int releaseExpired() {
        return repo.releaseExpired(OffsetDateTime.now());
    }
}
