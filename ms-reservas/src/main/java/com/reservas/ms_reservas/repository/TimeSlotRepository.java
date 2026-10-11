package com.reservas.ms_reservas.repository;

import com.reservas.commons.entity.TimeSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TimeSlot t where t.id = :id")
    Optional<TimeSlot> findByIdForUpdate(@Param("id") UUID id);
}
