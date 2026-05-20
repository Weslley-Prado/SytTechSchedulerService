package com.syttech.syttech.scheduler.scheduler.usecase;

import java.util.List;

import com.syttech.syttech.scheduler.scheduler.domain.command.AvailabilityQuery;
import com.syttech.syttech.scheduler.scheduler.domain.model.AvailabilitySlot;
import com.syttech.syttech.scheduler.scheduler.ports.in.GetAvailabilityUseCase;
import com.syttech.syttech.scheduler.scheduler.ports.out.AvailabilityQueryPort;
import com.syttech.syttech.scheduler.shared.kernel.DomainValidationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetAvailabilityUseCaseImpl implements GetAvailabilityUseCase {

    private final AvailabilityQueryPort availability;

    public GetAvailabilityUseCaseImpl(final AvailabilityQueryPort availability) {
        this.availability = availability;
    }

    @Override
    public List<AvailabilitySlot> getAvailability(final AvailabilityQuery query) {
        if (query.from() == null || query.to() == null || !query.to().isAfter(query.from())) {
            throw new DomainValidationException("'to' must be strictly after 'from'");
        }
        return availability.findAvailableSlots(query);
    }
}
