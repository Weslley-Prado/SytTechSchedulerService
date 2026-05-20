package com.syttech.syttech.scheduler.scheduler.usecase;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.syttech.syttech.scheduler.scheduler.domain.command.AvailabilityQuery;
import com.syttech.syttech.scheduler.scheduler.domain.command.CreateHoldCommand;
import com.syttech.syttech.scheduler.scheduler.domain.model.AvailabilitySlot;
import com.syttech.syttech.scheduler.scheduler.domain.model.Hold;
import com.syttech.syttech.scheduler.scheduler.ports.in.CreateHoldUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AvailabilityQueryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.HoldRepositoryPort;
import com.syttech.syttech.scheduler.scheduler.ports.out.UnitCatalogPort;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;
import com.syttech.syttech.scheduler.shared.kernel.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateHoldUseCaseImpl implements CreateHoldUseCase {

    private final HoldRepositoryPort holds;
    private final UnitCatalogPort catalog;
    private final AvailabilityQueryPort availability;
    private final Duration ttl;

    public CreateHoldUseCaseImpl(
            final HoldRepositoryPort holds,
            final UnitCatalogPort catalog,
            final AvailabilityQueryPort availability,
            @Value("${scheduler.hold.ttl-minutes:10}") final long ttlMinutes) {
        this.holds = holds;
        this.catalog = catalog;
        this.availability = availability;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    @Override
    public Hold createHold(final CreateHoldCommand command) {
        var service =
                catalog.findServiceById(command.serviceId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Service not found: " + command.serviceId()));
        if (command.start() == null) {
            throw new DomainValidationException("'start' is required");
        }
        var end = command.start().plusMinutes(service.durationMinutes());
        var professionalId = command.professionalId();
        if (professionalId == null) {
            professionalId = pickAnyAvailable(command, end);
        }
        var hold =
                new Hold(
                        UUID.randomUUID(),
                        command.unitId(),
                        command.serviceId(),
                        professionalId,
                        command.start(),
                        end,
                        OffsetDateTime.now().plus(ttl),
                        false);
        return holds.save(hold);
    }

    private UUID pickAnyAvailable(final CreateHoldCommand command, final OffsetDateTime end) {
        var slots =
                availability.findAvailableSlots(
                        new AvailabilityQuery(
                                command.unitId(),
                                command.serviceId(),
                                null,
                                command.start(),
                                end,
                                null));
        return slots.stream()
                .filter(s -> s.start().isEqual(command.start()))
                .map(AvailabilitySlot::professionalId)
                .findFirst()
                .orElseThrow(
                        () ->
                                new DomainValidationException(
                                        "No professional available for the requested slot"));
    }
}
