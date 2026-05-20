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
import com.syttech.syttech.scheduler.shared.kernel.UnauthenticatedException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseImplTest {

    @Mock TokenVerifierPort verifier;
    @Mock CustomerRepositoryPort customers;
    @Mock TokenIssuerPort tokens;

    @Test
    void issuesNewPairOnValidRefresh() {
        var customerId = UUID.randomUUID();
        var customer =
                new Customer(
                        customerId, "F", "a@x", null, "h", true, null, null, OffsetDateTime.now());
        when(verifier.verifyRefresh("RT")).thenReturn(Optional.of(customerId));
        when(customers.findById(customerId)).thenReturn(Optional.of(customer));
        var result = new LoginResult(customerId, "F", "a@x", true, "AT2", "RT2", 3600);
        when(tokens.issueFor(customer)).thenReturn(result);

        assertThat(
                        new RefreshTokenUseCaseImpl(verifier, customers, tokens)
                                .refresh(new RefreshTokenCommand("RT")))
                .isSameAs(result);
    }

    @Test
    void rejectsInvalidRefresh() {
        when(verifier.verifyRefresh("BAD")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new RefreshTokenUseCaseImpl(verifier, customers, tokens)
                                        .refresh(new RefreshTokenCommand("BAD")))
                .isInstanceOf(UnauthenticatedException.class);
    }

    @Test
    void rejectsWhenCustomerDeleted() {
        var customerId = UUID.randomUUID();
        when(verifier.verifyRefresh("RT")).thenReturn(Optional.of(customerId));
        when(customers.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new RefreshTokenUseCaseImpl(verifier, customers, tokens)
                                        .refresh(new RefreshTokenCommand("RT")))
                .isInstanceOf(UnauthenticatedException.class);
    }
}
