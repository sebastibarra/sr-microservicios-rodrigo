package com.reservas.ms_notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada (cron) que envía por correo las notificaciones en estado PENDING.
 *
 * <p>Las notificaciones las encolan otros servicios (por ejemplo ms-reservas al confirmar o cancelar
 * una reserva) o el endpoint {@code POST /notifications}. Por defecto corre cada 2 minutos
 * ({@code NOTIFICATION_DISPATCH_CRON}); el administrador también puede dispararla a mano con
 * {@code POST /notifications/dispatch}.</p>
 */
@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationService notifications;

    public NotificationDispatcher(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Scheduled(cron = "${app.notifications.dispatch-cron:0 */2 * * * *}")
    public void dispatchPendingOnSchedule() {
        try {
            NotificationService.DispatchResult result = notifications.dispatchPending();
            if (result.processed() > 0) {
                log.info("Scheduled dispatch: {} processed, {} sent, {} failed, {} still pending",
                        result.processed(), result.sent(), result.failed(), result.remaining());
            } else {
                log.debug("Scheduled dispatch: no pending notifications");
            }
        } catch (RuntimeException e) {
            log.error("Scheduled notification dispatch failed", e);
        }
    }
}
