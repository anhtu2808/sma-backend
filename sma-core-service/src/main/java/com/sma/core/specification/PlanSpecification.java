package com.sma.core.specification;

import com.sma.core.entity.Plan;
import com.sma.core.enums.PlanTarget;
import com.sma.core.enums.PlanType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PlanSpecification {

    public static Specification<Plan> withFilter(
            String name,
            PlanTarget planTarget,
            PlanType planType,
            Boolean isActive,
            Boolean isPopular
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                String pattern = "%" + name.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (planTarget != null) {
                predicates.add(cb.equal(root.get("planTarget"), planTarget));
            }

            if (planType != null) {
                predicates.add(cb.equal(root.get("planType"), planType));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (isPopular != null) {
                predicates.add(cb.equal(root.get("isPopular"), isPopular));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
