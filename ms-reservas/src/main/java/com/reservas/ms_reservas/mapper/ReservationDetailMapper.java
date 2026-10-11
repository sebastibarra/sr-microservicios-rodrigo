package com.reservas.ms_reservas.mapper;

import com.reservas.commons.dto.ReservationDetailCreateDto;
import com.reservas.commons.dto.ReservationDetailDto;
import com.reservas.commons.entity.ConsultationType;
import com.reservas.commons.entity.Reservation;
import com.reservas.commons.entity.ReservationDetail;
import org.springframework.stereotype.Component;

@Component
public class ReservationDetailMapper {

    private final ConsultationTypeMapper consultationTypeMapper;

    public ReservationDetailMapper(ConsultationTypeMapper consultationTypeMapper) {
        this.consultationTypeMapper = consultationTypeMapper;
    }

    public ReservationDetailDto toDto(ReservationDetail entity) {
        return new ReservationDetailDto()
                .id(entity.getId())
                .consultationType(consultationTypeMapper.toDto(entity.getConsultationType()))
                .comment(entity.getComment());
    }

    public ReservationDetail toEntity(
            ReservationDetailCreateDto dto,
            Reservation reservation,
            ConsultationType consultationType
    ) {
        ReservationDetail detail = new ReservationDetail();
        detail.setReservation(reservation);
        updateEntity(detail, dto, consultationType);
        return detail;
    }

    public void updateEntity(ReservationDetail detail, ReservationDetailCreateDto dto, ConsultationType consultationType) {
        detail.setConsultationType(consultationType);
        detail.setComment(dto.getComment());
    }
}
