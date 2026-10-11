package com.reservas.ms_reservas.mapper;

import com.reservas.commons.dto.TimeSlotDto;
import com.reservas.commons.dto.TimeSlotStatusDto;
import com.reservas.commons.entity.TimeSlot;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {

    public TimeSlotDto toDto(TimeSlot entity) {
        return new TimeSlotDto()
                .id(entity.getId())
                .professorId(entity.getProfessor().getId())
                .slotDate(entity.getSlotDate())
                .startTime(entity.getStartTime().toString())
                .endTime(entity.getEndTime().toString())
                .status(TimeSlotStatusDto.fromValue(entity.getStatus().name()));
    }
}
