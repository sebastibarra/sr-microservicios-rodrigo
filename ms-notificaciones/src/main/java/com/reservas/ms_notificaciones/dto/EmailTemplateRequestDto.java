package com.reservas.ms_notificaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmailTemplateRequestDto(
        @NotBlank @Size(max = 80) @Pattern(regexp = "[A-Z0-9_]+", message = "must contain only A-Z, 0-9 and _") String code,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank String htmlBody,
        Boolean active
) {
}
