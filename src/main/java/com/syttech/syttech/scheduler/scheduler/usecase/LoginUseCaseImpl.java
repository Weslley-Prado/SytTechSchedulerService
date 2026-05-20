package com.syttech.syttech.scheduler.scheduler.usecase;

import com.syttech.syttech.scheduler.scheduler.domain.command.LoginCommand;
import com.syttech.syttech.scheduler.scheduler.domain.command.LoginResult;
import com.syttech.syttech.scheduler.scheduler.domain.model.Customer;
import com.syttech.syttech.scheduler.scheduler.ports.in.LoginUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.CustomerRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.PasswordHasherPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.TokenIssuerPort;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginUseCaseImpl implements LoginUseCase {

    private final CustomerRepositoryPort customers;
    private final PasswordHasherPort hasher;
    private final TokenIssuerPort tokens;

    public LoginUseCaseImpl(
            final CustomerRepositoryPort customers,
            final PasswordHasherPort hasher,
            final TokenIssuerPort tokens) {
        this.customers = customers;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    @Override
    public LoginResult login(final LoginCommand command) {
        Customer customer =
                customers
                        .findByEmail(command.email())
                        .orElseThrow(() -> new DomainValidationException("Invalid credentials"));
        if (!hasher.matches(command.password(), customer.passwordHash())) {
            throw new DomainValidationException("Invalid credentials");
        }
        return tokens.issueFor(customer);
    }
}
