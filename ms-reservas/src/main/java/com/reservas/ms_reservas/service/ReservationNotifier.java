package com.reservas.ms_reservas.service;

import com.reservas.commons.entity.Notification;
import com.reservas.commons.entity.Reservation;
import com.reservas.commons.entity.ReservationDetail;
import com.reservas.commons.entity.TimeSlot;
import com.reservas.commons.entity.User;
import com.reservas.commons.enums.NotificationStatus;
import com.reservas.commons.enums.NotificationType;
import com.reservas.ms_reservas.repository.NotificationOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encola las notificaciones por correo de una reserva para el estudiante y para el profesor.
 *
 * <p>Patrón <em>transactional outbox</em>: las filas se insertan en estado {@code PENDING} dentro de
 * la misma transacción que crea o cancela la reserva. Si la reserva se revierte, las notificaciones
 * también; si se consuma, quedan pendientes aunque el servidor de correo no esté disponible.
 * ms-notificaciones las envía después con su tarea programada.</p>
 */
@Component
public class ReservationNotifier {

    private static final Logger log = LoggerFactory.getLogger(ReservationNotifier.class);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.of("es", "PY"));
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationOutboxRepository outbox;

    public ReservationNotifier(NotificationOutboxRepository outbox) {
        this.outbox = outbox;
    }

    /** Debe llamarse dentro de la transacción que confirmó la reserva. */
    public void reservationConfirmed(Reservation reservation) {
        enqueue(reservation, NotificationType.RESERVATION_CONFIRMATION);
    }

    /** Debe llamarse dentro de la transacción que canceló la reserva. */
    public void reservationCancelled(Reservation reservation) {
        enqueue(reservation, NotificationType.CANCELLATION);
    }

    private void enqueue(Reservation reservation, NotificationType type) {
        Map<String, Object> common = payload(reservation);
        TimeSlot slot = reservation.getTimeSlot();
        outbox.save(notification(reservation.getStudent(), "estudiante", type, common));
        outbox.save(notification(slot.getProfessor(), "profesor", type, common));
        log.info("Queued {} notifications (PENDING) for reservation {} to student and professor", type, reservation.getId());
    }

    private static Notification notification(User recipient, String role, NotificationType type, Map<String, Object> common) {
        Map<String, Object> payload = new HashMap<>(common);
        payload.put("recipientName", fullName(recipient));
        payload.put("recipientRole", role);
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setType(type);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setPayload(payload);
        return notification;
    }

    private static Map<String, Object> payload(Reservation reservation) {
        TimeSlot slot = reservation.getTimeSlot();
        String types = reservation.getDetails().stream()
                .map(ReservationDetail::getConsultationType)
                .map(type -> type.getName())
                .collect(Collectors.joining(", "));
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", String.valueOf(reservation.getId()));
        payload.put("studentName", fullName(reservation.getStudent()));
        payload.put("studentEmail", reservation.getStudent().getEmail());
        payload.put("professorName", fullName(slot.getProfessor()));
        payload.put("professorEmail", slot.getProfessor().getEmail());
        payload.put("date", DATE.format(slot.getSlotDate()));
        payload.put("startTime", TIME.format(slot.getStartTime()));
        payload.put("endTime", TIME.format(slot.getEndTime()));
        payload.put("consultationTypes", types);
        return payload;
    }

    private static String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
