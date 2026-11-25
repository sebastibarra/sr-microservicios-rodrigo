package com.reservas.ms_reservas.repository;

import com.reservas.commons.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Bandeja de salida (outbox) de notificaciones. ms-reservas solo inserta filas en estado PENDING;
 * el envío real lo hace ms-notificaciones con su tarea programada.
 */
@Repository
public interface NotificationOutboxRepository extends JpaRepository<Notification, UUID> {
}
