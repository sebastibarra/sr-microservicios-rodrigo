package com.reservas.ms_notificaciones.repository.spec;

import com.reservas.commons.entity.EmailTemplate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class EmailTemplateSpecifications {

    private EmailTemplateSpecifications() {
    }

    public static Specification<EmailTemplate> byFilters(String text, Boolean active) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(text)) {
                String pattern = "%" + text.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("subject")), pattern)
                ));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
