package com.reservas.ms_notificaciones.dto;

import com.reservas.commons.dto.PageMetadataDto;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(List<T> content, PageMetadataDto page) {

    public static <T> PageResponseDto<T> from(Page<T> source) {
        PageMetadataDto metadata = new PageMetadataDto()
                .page(source.getNumber())
                .size(source.getSize())
                .totalElements(source.getTotalElements())
                .totalPages(source.getTotalPages());
        return new PageResponseDto<>(source.getContent(), metadata);
    }
}
