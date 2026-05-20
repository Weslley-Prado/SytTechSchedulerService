package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence;

import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository.CustomerJpaRepository;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.ports.out.CustomerRepositoryPort;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class CustomerRepositoryJpaAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository repo;

    public CustomerRepositoryJpaAdapter(final CustomerJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Customer save(final Customer customer) {
        var existing = repo.findById(customer.id()).orElse(null);
        var entity = PersistenceMapper.toEntity(customer, existing);
        return PersistenceMapper.toDomain(repo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(final UUID customerId) {
        return repo.findById(customerId).map(PersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(final String email) {
        return repo.findByEmail(email).map(PersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByVerifyToken(final String token) {
        return repo.findByEmailVerifyToken(token).map(PersistenceMapper::toDomain);
    }
}
