package com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.adapter.output.persistence.entity.CustomerEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
    Optional<CustomerEntity> findByEmail(String email);

    Optional<CustomerEntity> findByEmailVerifyToken(String token);
}
