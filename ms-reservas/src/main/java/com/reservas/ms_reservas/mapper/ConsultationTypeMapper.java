package com.reservas.ms_reservas.mapper;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.commons.entity.ConsultationType;
import com.reservas.ms_reservas.dto.ConsultationTypeRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ConsultationTypeMapper {

    public CatalogDto toDto(ConsultationType entity) {
        return new CatalogDto()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.isActive());
    }

    public ConsultationType toEntity(ConsultationTypeRequestDto request) {
        ConsultationType entity = new ConsultationType();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(ConsultationType entity, ConsultationTypeRequestDto request) {
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        if (request.active() != null) {
            entity.setActive(request.active());
        }
    }
}
