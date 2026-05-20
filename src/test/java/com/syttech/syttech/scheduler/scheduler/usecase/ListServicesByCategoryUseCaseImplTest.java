package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListServicesByCategoryUseCaseImplTest {

    @Mock UnitCatalogPort catalog;

    @Test
    void delegatesToCatalog() {
        var unitId = UUID.randomUUID();
        var catId = UUID.randomUUID();
        var svcs =
                List.of(
                        new Service(
                                UUID.randomUUID(),
                                catId,
                                "Cut",
                                null,
                                30,
                                BigDecimal.TEN,
                                "BRL",
                                true));
        when(catalog.findServicesByCategory(unitId, catId)).thenReturn(svcs);

        assertThat(
                        new ListServicesByCategoryUseCaseImpl(catalog)
                                .listServicesByCategory(unitId, catId))
                .isSameAs(svcs);
    }
}
