package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateHoldUseCaseImplTest {

    @Mock HoldRepositoryPort holds;
    @Mock UnitCatalogPort catalog;
    @Mock AvailabilityQueryPort availability;

    @Test
    void createsHoldUsingExplicitProfessional() {
        var unitId = UUID.randomUUID();
        var svcId = UUID.randomUUID();
        var profId = UUID.randomUUID();
        var start = OffsetDateTime.now().plusHours(1);
        when(catalog.findServiceById(svcId))
                .thenReturn(
                        Optional.of(
                                new Service(
                                        svcId,
                                        UUID.randomUUID(),
                                        "Cut",
                                        null,
                                        30,
                                        BigDecimal.TEN,
                                        "BRL",
                                        true)));
        when(holds.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result =
                new CreateHoldUseCaseImpl(holds, catalog, availability, 10)
                        .createHold(new CreateHoldCommand(unitId, svcId, profId, start));

        assertThat(result.professionalId()).isEqualTo(profId);
        assertThat(result.end()).isEqualTo(start.plusMinutes(30));
        assertThat(result.consumed()).isFalse();
    }

    @Test
    void picksAvailableProfessionalWhenNotProvided() {
        var unitId = UUID.randomUUID();
        var svcId = UUID.randomUUID();
        var anyProf = UUID.randomUUID();
        var start = OffsetDateTime.now().plusHours(1);
        when(catalog.findServiceById(svcId))
                .thenReturn(
                        Optional.of(
                                new Service(
                                        svcId,
                                        UUID.randomUUID(),
                                        "Cut",
                                        null,
                                        30,
                                        BigDecimal.TEN,
                                        "BRL",
                                        true)));
        when(availability.findAvailableSlots(any()))
                .thenReturn(List.of(new AvailabilitySlot(start, start.plusMinutes(30), anyProf)));
        when(holds.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result =
                new CreateHoldUseCaseImpl(holds, catalog, availability, 10)
                        .createHold(new CreateHoldCommand(unitId, svcId, null, start));

        assertThat(result.professionalId()).isEqualTo(anyProf);
    }

    @Test
    void throwsWhenServiceMissing() {
        var svcId = UUID.randomUUID();
        when(catalog.findServiceById(svcId)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                new CreateHoldUseCaseImpl(holds, catalog, availability, 10)
                                        .createHold(
                                                new CreateHoldCommand(
                                                        UUID.randomUUID(),
                                                        svcId,
                                                        null,
                                                        OffsetDateTime.now())))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
