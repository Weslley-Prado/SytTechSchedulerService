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
class ListProfessionalsByServiceUseCaseImplTest {

    @Mock UnitCatalogPort catalog;

    @Test
    void delegatesToCatalog() {
        var unitId = UUID.randomUUID();
        var svcId = UUID.randomUUID();
        var list = List.of(new Professional(UUID.randomUUID(), unitId, "Ada", null, 4.7f, true));
        when(catalog.findProfessionalsByService(unitId, svcId)).thenReturn(list);

        assertThat(
                        new ListProfessionalsByServiceUseCaseImpl(catalog)
                                .listProfessionalsByService(unitId, svcId))
                .isSameAs(list);
    }
}
