package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
class ListCategoriesByUnitUseCaseImplTest {

    @Mock UnitCatalogPort catalog;

    @Test
    void delegatesToCatalog() {
        var unitId = UUID.randomUUID();
        var list = List.of(new Category(UUID.randomUUID(), unitId, "Hair", null));
        when(catalog.findCategoriesByUnit(unitId)).thenReturn(list);

        assertThat(new ListCategoriesByUnitUseCaseImpl(catalog).listCategoriesByUnit(unitId))
                .isSameAs(list);
    }
}
