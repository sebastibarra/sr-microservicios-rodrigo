package com.reservas.ms_reservas.dto;

import com.reservas.commons.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationFilter(
        UUID studentId,
        UUID professorId,
        UUID consultationTypeId,
        ReservationStatus status,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}
