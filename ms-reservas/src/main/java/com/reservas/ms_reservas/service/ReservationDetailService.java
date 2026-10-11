package com.reservas.ms_reservas.service;

import com.reservas.commons.dto.ReservationDetailCreateDto;
import com.reservas.commons.dto.ReservationDetailDto;
import com.reservas.commons.entity.ConsultationType;
import com.reservas.commons.entity.Reservation;
import com.reservas.commons.entity.ReservationDetail;
import com.reservas.commons.exception.ConflictException;
import com.reservas.commons.exception.InvalidOperationException;
import com.reservas.commons.exception.ResourceNotFoundException;
import com.reservas.ms_reservas.mapper.ReservationDetailMapper;
import com.reservas.ms_reservas.repository.ReservationDetailRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReservationDetailService extends BaseService<ReservationDetail> {

    private final ReservationDetailRepository repository;
    private final ReservationDetailMapper mapper;
    private final ReservationService reservationService;
    private final ConsultationTypeService consultationTypeService;

    public ReservationDetailService(
            ReservationDetailRepository repository,
            ReservationDetailMapper mapper,
            ReservationService reservationService,
            ConsultationTypeService consultationTypeService
    ) {
        super(repository, "Reservation detail");
        this.repository = repository;
        this.mapper = mapper;
        this.reservationService = reservationService;
        this.consultationTypeService = consultationTypeService;
    }

    @Transactional(readOnly = true)
    public Page<ReservationDetailDto> list(UUID reservationId, Pageable pageable) {
        reservationService.get(reservationId);
        log.debug("Listing details of reservation {}", reservationId);
        return repository.findByReservationId(reservationId, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public ReservationDetailDto get(UUID reservationId, UUID detailId) {
        return mapper.toDto(findInReservation(reservationId, detailId));
    }

    @Transactional
    public ReservationDetailDto add(UUID reservationId, ReservationDetailCreateDto request) {
        log.info("Adding consultation type {} to reservation {}", request.getConsultationTypeId(), reservationId);
        reservationService.validateComment(request.getComment());
        Reservation reservation = reservationService.getModifiableEntity(reservationId);
        ConsultationType type = consultationTypeService.getActiveEntity(request.getConsultationTypeId());
        assertNotAssociated(reservationId, type.getId());
        return mapper.toDto(repository.save(mapper.toEntity(request, reservation, type)));
    }

    @Transactional
    public ReservationDetailDto update(UUID reservationId, UUID detailId, ReservationDetailCreateDto request) {
        log.info("Updating detail {} of reservation {}", detailId, reservationId);
        reservationService.validateComment(request.getComment());
        reservationService.getModifiableEntity(reservationId);
        ReservationDetail detail = findInReservation(reservationId, detailId);
        ConsultationType type = consultationTypeService.getActiveEntity(request.getConsultationTypeId());
        if (!detail.getConsultationType().getId().equals(type.getId())) {
            assertNotAssociated(reservationId, type.getId());
        }
        mapper.updateEntity(detail, request, type);
        return mapper.toDto(repository.save(detail));
    }

    @Transactional
    public void remove(UUID reservationId, UUID detailId) {
        log.info("Removing detail {} from reservation {}", detailId, reservationId);
        reservationService.getModifiableEntity(reservationId);
        ReservationDetail detail = findInReservation(reservationId, detailId);
        if (repository.countByReservationId(reservationId) <= 1) {
            throw new InvalidOperationException("A reservation must keep at least one consultation type");
        }
        repository.delete(detail);
    }

    private ReservationDetail findInReservation(UUID reservationId, UUID detailId) {
        return repository.findByIdAndReservationId(detailId, reservationId).orElseThrow(() -> {
            log.warn("Detail {} not found in reservation {}", detailId, reservationId);
            return new ResourceNotFoundException("Reservation detail not found: " + detailId);
        });
    }

    private void assertNotAssociated(UUID reservationId, UUID consultationTypeId) {
        if (repository.existsByReservationIdAndConsultationTypeId(reservationId, consultationTypeId)) {
            throw new ConflictException("DUPLICATE_RESERVATION_DETAIL", "The consultation type is already part of the reservation");
        }
    }
}
