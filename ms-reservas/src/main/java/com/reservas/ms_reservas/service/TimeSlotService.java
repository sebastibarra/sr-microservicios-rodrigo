package com.reservas.ms_reservas.service;

import com.reservas.commons.entity.TimeSlot;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.commons.exception.TimeSlotUnavailableException;
import com.reservas.commons.exception.UnprocessableEntityException;
import com.reservas.ms_reservas.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TimeSlotService extends BaseService<TimeSlot> {

    private final TimeSlotRepository repository;
    private final Clock clock;

    public TimeSlotService(TimeSlotRepository repository, Clock clock) {
        super(repository, "Time slot");
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public TimeSlot lockForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Time slot {} not found", id);
            return new ResourceNotFoundException("Time slot not found: " + id);
        });
    }

    public void assertBookable(TimeSlot slot, int minAdvanceHours) {
        if (!slot.isAvailable()) {
            log.warn("Time slot {} is {}, booking rejected", slot.getId(), slot.getStatus());
            throw new TimeSlotUnavailableException("The time slot is not available, please choose another one");
        }
        LocalDateTime limit = LocalDateTime.now(clock).plusHours(minAdvanceHours);
        if (startOf(slot).isBefore(limit)) {
            throw new UnprocessableEntityException("BOOKING_TOO_SOON",
                    "Reservations require at least " + minAdvanceHours + " hours of advance notice");
        }
    }

    public boolean hasStarted(TimeSlot slot) {
        return !startOf(slot).isAfter(LocalDateTime.now(clock));
    }

    private LocalDateTime startOf(TimeSlot slot) {
        return LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
    }
}
