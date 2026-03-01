package com.sma.core.specification;

import com.sma.core.entity.UsageEvent;
import com.sma.core.enums.FeatureKey;
import com.sma.core.enums.EventSource;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsageEventSpecification {

    public static Specification<UsageEvent> filterBy(
            FeatureKey featureKey,
            LocalDateTime startDate,
            LocalDateTime endDate,
            EventSource entityType,
            Integer entityId,
            List<Integer> subscriptionIds) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("subscription").get("id").in(subscriptionIds));

            if (featureKey != null) {
                predicates.add(cb.equal(root.get("feature").get("featureKey"), featureKey));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }

            // Filter directly on eventSource and sourceId columns
            if (entityType != null) {
                predicates.add(cb.equal(root.get("eventSource"), entityType));
            }

            if (entityId != null) {
                predicates.add(cb.equal(root.get("sourceId"), entityId));
            }

            query.distinct(true);
            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
