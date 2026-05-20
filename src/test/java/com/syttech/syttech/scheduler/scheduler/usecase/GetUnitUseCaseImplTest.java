package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
class GetUnitUseCaseImplTest {

    @Mock UnitCatalogPort catalog;

    @Test
    void returnsUnit() {
        var id = UUID.randomUUID();
        var unit = new Unit(id, "u", null, null, null, null, null, List.of(), true);
        when(catalog.findUnitById(id)).thenReturn(Optional.of(unit));

        assertThat(new GetUnitUseCaseImpl(catalog).getUnit(id)).isSameAs(unit);
    }

    @Test
    void throws404WhenMissing() {
        var id = UUID.randomUUID();
        when(catalog.findUnitById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GetUnitUseCaseImpl(catalog).getUnit(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
