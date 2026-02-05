package com.sma.core.specification;

import com.sma.core.dto.request.company.CompanyFilterRequest;
import com.sma.core.entity.Company;
import com.sma.core.entity.CompanyLocation;
import com.sma.core.enums.CompanyStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CompanySpecification {

    public static Specification<Company> withFilter(
            CompanyFilterRequest request,
            EnumSet<CompanyStatus> allowedStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getName())) {
                String pattern = "%" + request.getName().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (!CollectionUtils.isEmpty(request.getLocation())) {
                Join<Company, CompanyLocation> locationJoin =
                        root.join("locations", JoinType.INNER);
                List<Predicate> locationPredicates = new ArrayList<>();
                for (String keyword : request.getLocation()) {
                    String pattern = "%" + keyword.toLowerCase() + "%";
                    locationPredicates.add(cb.like(cb.lower(locationJoin.get("city")), pattern));
                }
                predicates.add(cb.or(locationPredicates.toArray(new Predicate[0])));
            }

            if (request.getType() != null)
                predicates.add(root.get("type").in(request.getType()));
            if (request.getIndustry() != null)
                predicates.add(root.get("industry").in(request.getIndustry()));

            if (!allowedStatus.isEmpty())
                predicates.add(root.get("status").in(allowedStatus));

            assert query != null;
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
