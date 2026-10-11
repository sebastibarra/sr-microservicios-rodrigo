package com.reservas.ms_reservas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsultationTypeRequestDto(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        Boolean active
) {
}
