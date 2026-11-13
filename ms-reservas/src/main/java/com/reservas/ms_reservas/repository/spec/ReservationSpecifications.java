package com.reservas.ms_reservas.repository.spec;

import com.reservas.commons.entity.Reservation;
import com.reservas.commons.entity.ReservationDetail;
import com.reservas.ms_reservas.dto.ReservationFilter;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ReservationSpecifications {

    private ReservationSpecifications() {
    }

    public static Specification<Reservation> byFilter(ReservationFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.studentId() != null) {
                predicates.add(cb.equal(root.get("student").get("id"), filter.studentId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.professorId() != null) {
                predicates.add(cb.equal(root.get("timeSlot").get("professor").get("id"), filter.professorId()));
            }
            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timeSlot").get("slotDate"), filter.dateFrom()));
            }
            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timeSlot").get("slotDate"), filter.dateTo()));
            }
            if (filter.consultationTypeId() != null) {
                // EXISTS en vez de JOIN + DISTINCT: permite ordenar por la fecha del turno sin duplicar filas.
                Subquery<ReservationDetail> byType = query.subquery(ReservationDetail.class);
                Root<ReservationDetail> detail = byType.from(ReservationDetail.class);
                byType.select(detail).where(
                        cb.equal(detail.get("reservation"), root),
                        cb.equal(detail.get("consultationType").get("id"), filter.consultationTypeId()));
                predicates.add(cb.exists(byType));
            }
            if (filter.q() != null && !filter.q().isBlank()) {
                // Texto libre: nombre del estudiante, del profesor o del tipo de consulta.
                String like = "%" + filter.q().trim().toLowerCase() + "%";
                var student = root.join("student", JoinType.INNER);
                var professor = root.join("timeSlot", JoinType.INNER).join("professor", JoinType.INNER);
                Subquery<ReservationDetail> byTypeName = query.subquery(ReservationDetail.class);
                Root<ReservationDetail> detail = byTypeName.from(ReservationDetail.class);
                byTypeName.select(detail).where(
                        cb.equal(detail.get("reservation"), root),
                        cb.like(cb.lower(detail.get("consultationType").get("name")), like));
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.concat(cb.concat(student.get("firstName"), " "), student.get("lastName"))), like),
                        cb.like(cb.lower(cb.concat(cb.concat(professor.get("firstName"), " "), professor.get("lastName"))), like),
                        cb.exists(byTypeName)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
