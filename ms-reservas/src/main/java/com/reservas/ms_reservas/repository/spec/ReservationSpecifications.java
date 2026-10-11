package com.reservas.ms_reservas.repository.spec;

import com.reservas.commons.entity.Reservation;
import com.reservas.ms_reservas.dto.ReservationFilter;
import jakarta.persistence.criteria.Predicate;
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
                query.distinct(true);
                predicates.add(cb.equal(root.join("details").get("consultationType").get("id"), filter.consultationTypeId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
