package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.syttech.syttech.scheduler.scheduler.domain.command.*;
import com.syttech.syttech.scheduler.scheduler.domain.model.*;
import com.syttech.syttech.scheduler.scheduler.ports.out.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListUnitsUseCaseImplTest {

    @Mock UnitCatalogPort catalog;

    @Test
    void delegatesToCatalog() {
        var query = new ListUnitsQuery("haircut", "Sao Paulo", 0, 20);
        var page = new PageResult<Unit>(List.of(), 0, 20, 0, 0);
        when(catalog.findUnits(query)).thenReturn(page);

        var result = new ListUnitsUseCaseImpl(catalog).listUnits(query);

        assertThat(result).isSameAs(page);
        verify(catalog).findUnits(query);
    }
}
