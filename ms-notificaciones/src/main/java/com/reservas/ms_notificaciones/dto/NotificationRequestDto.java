package com.reservas.ms_notificaciones.dto;

import com.reservas.commons.enums.NotificationType;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record NotificationRequestDto(
        @NotNull UUID userId,
        @NotNull NotificationType type,
        Map<String, Object> payload
) {
}
