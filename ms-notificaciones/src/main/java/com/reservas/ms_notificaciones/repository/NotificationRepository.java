package com.reservas.ms_notificaciones.repository;

import com.reservas.commons.entity.Notification;
import com.reservas.commons.enums.NotificationStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    /**
     * Toma un lote de notificaciones en el estado dado, las más antiguas primero, y las bloquea para
     * escritura. El timeout -2 hace que Hibernate genere {@code FOR UPDATE SKIP LOCKED} en PostgreSQL:
     * si dos instancias del servicio ejecutan el envío a la vez, cada una toma filas distintas y ningún
     * correo se manda dos veces.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    List<Notification> findByStatusOrderByCreatedAtAsc(NotificationStatus status, Limit limit);

    long countByStatus(NotificationStatus status);
}
