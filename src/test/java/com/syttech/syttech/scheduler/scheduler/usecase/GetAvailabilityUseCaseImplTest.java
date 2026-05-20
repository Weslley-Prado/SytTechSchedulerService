package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
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
class GetAvailabilityUseCaseImplTest {

    @Mock AvailabilityQueryPort availability;

    @Test
    void delegatesWhenRangeValid() {
        var now = OffsetDateTime.now();
        var q =
                new AvailabilityQuery(
                        UUID.randomUUID(), UUID.randomUUID(), null, now, now.plusHours(2), "UTC");
        var slot = new AvailabilitySlot(now, now.plusMinutes(30), UUID.randomUUID());
        when(availability.findAvailableSlots(q)).thenReturn(List.of(slot));

        assertThat(new GetAvailabilityUseCaseImpl(availability).getAvailability(q))
                .containsExactly(slot);
    }

    @Test
    void throwsWhenRangeInvalid() {
        var now = OffsetDateTime.now();
        var q = new AvailabilityQuery(UUID.randomUUID(), UUID.randomUUID(), null, now, now, "UTC");
        assertThatThrownBy(() -> new GetAvailabilityUseCaseImpl(availability).getAvailability(q))
                .isInstanceOf(DomainValidationException.class);
    }
}
