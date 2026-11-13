package com.reservas.ms_notificaciones.repository.spec;

import com.reservas.commons.entity.Notification;
import com.reservas.ms_notificaciones.dto.NotificationFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class NotificationSpecifications {

    private NotificationSpecifications() {
    }

    public static Specification<Notification> byFilter(NotificationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.userId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), filter.userId()));
            }
            if (filter.type() != null) {
                predicates.add(cb.equal(root.get("type"), filter.type()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.q() != null && !filter.q().isBlank()) {
                // Texto libre: nombre, apellido o correo del destinatario.
                String like = "%" + filter.q().trim().toLowerCase() + "%";
                var user = root.join("user");
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.concat(cb.concat(user.get("firstName"), " "), user.get("lastName"))), like),
                        cb.like(cb.lower(user.get("email")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
