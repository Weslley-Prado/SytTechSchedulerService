package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerifyCustomerEmailUseCaseImplTest {

    @Mock CustomerRepositoryPort customers;

    @Test
    void marksVerified() {
        var customer =
                new Customer(
                        UUID.randomUUID(),
                        "F",
                        "a@x",
                        null,
                        "h",
                        false,
                        "tok",
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now());
        when(customers.findByVerifyToken("tok")).thenReturn(Optional.of(customer));
        when(customers.save(any())).thenAnswer(inv -> inv.getArgument(0));

        new VerifyCustomerEmailUseCaseImpl(customers).verifyCustomerEmail("tok");

        verify(customers).save(any(Customer.class));
    }

    @Test
    void throws404WhenTokenUnknown() {
        when(customers.findByVerifyToken("x")).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new VerifyCustomerEmailUseCaseImpl(customers)
                                        .verifyCustomerEmail("x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsExpiredToken() {
        var customer =
                new Customer(
                        UUID.randomUUID(),
                        "F",
                        "a@x",
                        null,
                        "h",
                        false,
                        "tok",
                        OffsetDateTime.now().minusDays(1),
                        OffsetDateTime.now());
        when(customers.findByVerifyToken("tok")).thenReturn(Optional.of(customer));

        assertThatThrownBy(
                        () ->
                                new VerifyCustomerEmailUseCaseImpl(customers)
                                        .verifyCustomerEmail("tok"))
                .isInstanceOf(DomainValidationException.class);
    }
}
