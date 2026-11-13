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
                .professorName(fullName(entity.getProfessor()))
                .slotDate(entity.getSlotDate())
                .startTime(entity.getStartTime().toString())
                .endTime(entity.getEndTime().toString())
                .status(TimeSlotStatusDto.fromValue(entity.getStatus().name()));
    }

    /** Nombre legible para la UI (los listados no muestran UUIDs). */
    public static String fullName(com.reservas.commons.entity.User user) {
        if (user == null) {
            return null;
        }
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}
