package com.reservas.ms_reservas.mapper;

import com.reservas.commons.dto.ReservationCreateDto;
import com.reservas.commons.dto.ReservationDetailCreateDto;
import com.reservas.commons.dto.ReservationDto;
import com.reservas.commons.dto.ReservationStatusDto;
import com.reservas.commons.entity.ConsultationType;
import com.reservas.commons.entity.Reservation;
import com.reservas.commons.entity.Student;
import com.reservas.commons.entity.TimeSlot;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

@Component
public class ReservationMapper {

    private final TimeSlotMapper timeSlotMapper;
    private final ReservationDetailMapper detailMapper;

    public ReservationMapper(TimeSlotMapper timeSlotMapper, ReservationDetailMapper detailMapper) {
        this.timeSlotMapper = timeSlotMapper;
        this.detailMapper = detailMapper;
    }

    public ReservationDto toDto(Reservation entity) {
        return new ReservationDto()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .timeSlot(timeSlotMapper.toDto(entity.getTimeSlot()))
                .status(ReservationStatusDto.fromValue(entity.getStatus().name()))
                .details(entity.getDetails().stream()
                        .map(detailMapper::toDto)
                        .sorted(Comparator.comparing(detail -> detail.getConsultationType().getName()))
                        .toList());
    }

    public Reservation toEntity(
            ReservationCreateDto dto,
            Student student,
            TimeSlot timeSlot,
            Map<UUID, ConsultationType> consultationTypes
    ) {
        Reservation reservation = new Reservation();
        reservation.setStudent(student);
        reservation.setTimeSlot(timeSlot);
        for (ReservationDetailCreateDto detail : dto.getDetails()) {
            reservation.addDetail(consultationTypes.get(detail.getConsultationTypeId()), detail.getComment());
        }
        return reservation;
    }
}
