package com.syttech.syttech.scheduler.scheduler.usecase;

import java.time.OffsetDateTime;

import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.ports.in.VerifyCustomerEmailUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.CustomerRepositoryPort;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VerifyCustomerEmailUseCaseImpl implements VerifyCustomerEmailUseCase {

    private final CustomerRepositoryPort customers;

    public VerifyCustomerEmailUseCaseImpl(final CustomerRepositoryPort customers) {
        this.customers = customers;
    }

    @Override
    public void verifyCustomerEmail(final String token) {
        Customer customer =
                customers
                        .findByVerifyToken(token)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Verification token not found"));
        if (customer.emailVerifyExpiresAt() != null
                && customer.emailVerifyExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new DomainValidationException("Verification token expired");
        }
        customers.save(
                new Customer(
                        customer.id(),
                        customer.fullName(),
                        customer.email(),
                        customer.phone(),
                        customer.passwordHash(),
                        true,
                        null,
                        null,
                        customer.createdAt()));
    }
}
