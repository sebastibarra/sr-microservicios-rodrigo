package com.reservas.ms_reservas.repository;

import com.reservas.commons.entity.ReservationDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReservationDetailRepository extends JpaRepository<ReservationDetail, UUID> {

    Page<ReservationDetail> findByReservationId(UUID reservationId, Pageable pageable);

    Optional<ReservationDetail> findByIdAndReservationId(UUID id, UUID reservationId);

    long countByReservationId(UUID reservationId);

    boolean existsByReservationIdAndConsultationTypeId(UUID reservationId, UUID consultationTypeId);
}
