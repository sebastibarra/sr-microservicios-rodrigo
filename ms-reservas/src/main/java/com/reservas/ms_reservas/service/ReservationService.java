package com.reservas.ms_reservas.service;

import com.reservas.commons.dto.ReservationCreateDto;
import com.reservas.commons.dto.ReservationDetailCreateDto;
import com.reservas.commons.dto.ReservationDto;
import com.reservas.commons.entity.ConsultationType;
import com.reservas.commons.entity.Reservation;
import com.reservas.commons.entity.ReservationDetail;
import com.reservas.commons.entity.Student;
import com.reservas.commons.entity.TimeSlot;
import com.reservas.commons.enums.ReservationStatus;
import com.reservas.commons.exception.BadRequestException;
import com.reservas.commons.exception.InvalidOperationException;
import com.reservas.ms_reservas.config.BookingProperties;
import com.reservas.ms_reservas.dto.ReservationFilter;
import com.reservas.ms_reservas.mapper.ReservationMapper;
import com.reservas.ms_reservas.repository.ReservationRepository;
import com.reservas.ms_reservas.repository.spec.ReservationSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReservationService extends BaseService<Reservation> {

    private static final Set<ReservationStatus> MODIFIABLE = Set.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
    private static final int MAX_COMMENT_LENGTH = 500;

    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final StudentService studentService;
    private final TimeSlotService timeSlotService;
    private final ConsultationTypeService consultationTypeService;
    private final SettingService settingService;
    private final BookingProperties booking;

    public ReservationService(
            ReservationRepository repository,
            ReservationMapper mapper,
            StudentService studentService,
            TimeSlotService timeSlotService,
            ConsultationTypeService consultationTypeService,
            SettingService settingService,
            BookingProperties booking
    ) {
        super(repository, "Reservation");
        this.repository = repository;
        this.mapper = mapper;
        this.studentService = studentService;
        this.timeSlotService = timeSlotService;
        this.consultationTypeService = consultationTypeService;
        this.settingService = settingService;
        this.booking = booking;
    }

    @Transactional
    public ReservationDto create(ReservationCreateDto request) {
        log.info("Creating reservation student={} timeSlot={}", request.getStudentId(), request.getTimeSlotId());
        Set<UUID> typeIds = requireUniqueTypes(request.getDetails());
        Student student = studentService.getReservableStudent(request.getStudentId());
        Map<UUID, ConsultationType> types = consultationTypeService.getActiveEntities(typeIds);

        TimeSlot slot = timeSlotService.lockForUpdate(request.getTimeSlotId());
        timeSlotService.assertBookable(slot, minAdvanceHours());

        Reservation reservation = mapper.toEntity(request, student, slot, types);
        reservation.confirm();
        Reservation saved = repository.save(reservation);
        log.info("Reservation {} confirmed, time slot {} is now {}", saved.getId(), slot.getId(), slot.getStatus());
        return mapper.toDto(saved);
    }

    @Transactional
    public ReservationDto update(UUID id, ReservationCreateDto request) {
        log.info("Updating reservation {}", id);
        Reservation reservation = getModifiableEntity(id);
        if (!reservation.getStudent().getId().equals(request.getStudentId())) {
            throw new BadRequestException("The student of a reservation cannot be changed");
        }
        Set<UUID> typeIds = requireUniqueTypes(request.getDetails());
        Map<UUID, ConsultationType> types = consultationTypeService.getActiveEntities(typeIds);

        if (!reservation.getTimeSlot().getId().equals(request.getTimeSlotId())) {
            moveToTimeSlot(reservation, request.getTimeSlotId());
        }
        syncDetails(reservation, request.getDetails(), types);
        return mapper.toDto(repository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationDto get(UUID id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ReservationDto> list(Pageable pageable) {
        log.debug("Listing reservations page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ReservationDto> search(ReservationFilter filter, Pageable pageable) {
        log.debug("Searching reservations with {}", filter);
        return repository.findAll(ReservationSpecifications.byFilter(filter), pageable).map(mapper::toDto);
    }

    @Transactional
    public ReservationDto cancel(UUID id) {
        log.info("Cancelling reservation {}", id);
        Reservation reservation = findOrThrow(id);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            log.info("Reservation {} was already cancelled", id);
            return mapper.toDto(reservation);
        }
        if (reservation.getStatus() == ReservationStatus.CONFIRMED || reservation.getStatus() == ReservationStatus.PENDING) {
            if (timeSlotService.hasStarted(reservation.getTimeSlot())) {
                throw new InvalidOperationException("A reservation cannot be cancelled once its time slot has started");
            }
        }
        timeSlotService.lockForUpdate(reservation.getTimeSlot().getId());
        reservation.cancel();
        Reservation saved = repository.save(reservation);
        log.info("Reservation {} cancelled, time slot {} released", id, saved.getTimeSlot().getId());
        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Reservation getModifiableEntity(UUID id) {
        Reservation reservation = findOrThrow(id);
        if (!MODIFIABLE.contains(reservation.getStatus())) {
            throw new InvalidOperationException("A " + reservation.getStatus() + " reservation cannot be modified");
        }
        return reservation;
    }

    private void moveToTimeSlot(Reservation reservation, UUID newSlotId) {
        TimeSlot newSlot = timeSlotService.lockForUpdate(newSlotId);
        timeSlotService.assertBookable(newSlot, minAdvanceHours());
        TimeSlot oldSlot = reservation.getTimeSlot();
        newSlot.book();
        oldSlot.release();
        reservation.setTimeSlot(newSlot);
        log.info("Reservation {} moved from time slot {} to {}", reservation.getId(), oldSlot.getId(), newSlot.getId());
    }

    private void syncDetails(Reservation reservation, List<ReservationDetailCreateDto> requested, Map<UUID, ConsultationType> types) {
        Set<UUID> requestedIds = requested.stream().map(ReservationDetailCreateDto::getConsultationTypeId).collect(Collectors.toSet());
        reservation.getDetails().removeIf(detail -> !requestedIds.contains(detail.getConsultationType().getId()));
        Map<UUID, ReservationDetail> existing = reservation.getDetails().stream()
                .collect(Collectors.toMap(detail -> detail.getConsultationType().getId(), Function.identity()));
        for (ReservationDetailCreateDto dto : requested) {
            ReservationDetail current = existing.get(dto.getConsultationTypeId());
            if (current != null) {
                current.setComment(dto.getComment());
            } else {
                reservation.addDetail(types.get(dto.getConsultationTypeId()), dto.getComment());
            }
        }
    }

    private Set<UUID> requireUniqueTypes(List<ReservationDetailCreateDto> details) {
        if (details == null || details.isEmpty()) {
            throw new BadRequestException("A reservation requires at least one consultation type");
        }
        Set<UUID> ids = new HashSet<>();
        for (ReservationDetailCreateDto detail : details) {
            validateComment(detail.getComment());
            if (!ids.add(detail.getConsultationTypeId())) {
                throw new BadRequestException("Duplicated consultation type in the reservation: " + detail.getConsultationTypeId());
            }
        }
        return ids;
    }

    public void validateComment(String comment) {
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new BadRequestException("comment must have at most " + MAX_COMMENT_LENGTH + " characters");
        }
    }

    private int minAdvanceHours() {
        return settingService.getInt(booking.minAdvanceSettingKey(), booking.minAdvanceHours());
    }
}
