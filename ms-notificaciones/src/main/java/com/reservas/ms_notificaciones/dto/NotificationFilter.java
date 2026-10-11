package com.reservas.ms_notificaciones.dto;

import com.reservas.commons.enums.NotificationStatus;
import com.reservas.commons.enums.NotificationType;

import java.util.UUID;

public record NotificationFilter(UUID userId, NotificationType type, NotificationStatus status) {
}
