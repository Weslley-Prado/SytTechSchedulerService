package com.syttech.syttech.scheduler.scheduler.ports.out;

import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;

/** Persistence port for Customer accounts. */
public interface CustomerRepositoryPort {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID customerId);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByVerifyToken(String token);
}
