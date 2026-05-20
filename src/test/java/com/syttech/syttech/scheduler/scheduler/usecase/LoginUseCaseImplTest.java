package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock CustomerRepositoryPort customers;
    @Mock PasswordHasherPort hasher;
    @Mock TokenIssuerPort tokens;

    @Test
    void issuesTokensOnValidCredentials() {
        var customer =
                new Customer(
                        UUID.randomUUID(),
                        "F",
                        "a@x",
                        null,
                        "HASH",
                        true,
                        null,
                        null,
                        OffsetDateTime.now());
        when(customers.findByEmail("a@x")).thenReturn(Optional.of(customer));
        when(hasher.matches("pwd", "HASH")).thenReturn(true);
        var result = new LoginResult(customer.id(), "F", "a@x", true, "AT", "RT", 3600);
        when(tokens.issueFor(customer)).thenReturn(result);

        assertThat(
                        new LoginUseCaseImpl(customers, hasher, tokens)
                                .login(new LoginCommand("a@x", "pwd")))
                .isSameAs(result);
    }

    @Test
    void rejectsBadPassword() {
        var customer =
                new Customer(
                        UUID.randomUUID(),
                        "F",
                        "a@x",
                        null,
                        "HASH",
                        true,
                        null,
                        null,
                        OffsetDateTime.now());
        when(customers.findByEmail("a@x")).thenReturn(Optional.of(customer));
        when(hasher.matches("pwd", "HASH")).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                new LoginUseCaseImpl(customers, hasher, tokens)
                                        .login(new LoginCommand("a@x", "pwd")))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsUnknownEmail() {
        when(customers.findByEmail("a@x")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new LoginUseCaseImpl(customers, hasher, tokens)
                                        .login(new LoginCommand("a@x", "pwd")))
                .isInstanceOf(DomainValidationException.class);
    }
}
