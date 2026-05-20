package com.syttech.syttech.scheduler.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
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
class ReleaseHoldUseCaseImplTest {

    @Mock HoldRepositoryPort holds;

    @Test
    void deletesWhenFound() {
        var id = UUID.randomUUID();
        when(holds.findById(id))
                .thenReturn(
                        Optional.of(
                                new Hold(
                                        id,
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        OffsetDateTime.now(),
                                        OffsetDateTime.now().plusMinutes(30),
                                        OffsetDateTime.now().plusMinutes(10),
                                        false)));

        new ReleaseHoldUseCaseImpl(holds).releaseHold(id);

        verify(holds).deleteById(id);
    }

    @Test
    void throws404WhenMissing() {
        var id = UUID.randomUUID();
        when(holds.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ReleaseHoldUseCaseImpl(holds).releaseHold(id))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(holds, never()).deleteById(any());
    }
}
