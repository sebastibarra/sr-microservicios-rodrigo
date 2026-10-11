package com.reservas.ms_notificaciones.mapper;

import com.reservas.commons.dto.NotificationChannelDto;
import com.reservas.commons.dto.NotificationDto;
import com.reservas.commons.dto.NotificationStatusDto;
import com.reservas.commons.dto.NotificationTypeDto;
import com.reservas.commons.entity.Notification;
import com.reservas.commons.entity.User;
import com.reservas.ms_notificaciones.dto.NotificationRequestDto;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification entity) {
        return new NotificationDto()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .type(NotificationTypeDto.fromValue(entity.getType().name()))
                .channel(NotificationChannelDto.fromValue(entity.getChannel().name()))
                .status(NotificationStatusDto.fromValue(entity.getStatus().name()))
                .payload(entity.getPayload() == null ? new HashMap<>() : entity.getPayload())
                .sentAt(entity.getSentAt() == null ? null : OffsetDateTime.ofInstant(entity.getSentAt(), ZoneOffset.UTC))
                .errorMessage(entity.getErrorMessage());
    }

    public Notification toEntity(NotificationRequestDto request, User user) {
        Notification entity = new Notification();
        entity.setUser(user);
        entity.setType(request.type());
        entity.setPayload(request.payload() == null ? new HashMap<>() : new HashMap<>(request.payload()));
        return entity;
    }
}
