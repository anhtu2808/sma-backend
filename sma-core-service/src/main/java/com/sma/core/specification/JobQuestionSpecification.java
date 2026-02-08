package com.sma.core.specification;

import com.sma.core.dto.request.question.JobQuestionFilterRequest;
import com.sma.core.entity.Company;
import com.sma.core.entity.Job;
import com.sma.core.entity.JobQuestion;
import com.sma.core.entity.Skill;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobQuestionSpecification {

    public static Specification<JobQuestion> withFilter(
            JobQuestionFilterRequest request,
            Integer jobId,
            Integer companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by jobId if provided
            if (jobId != null) {
                predicates.add(cb.equal(root.get("job").get("id"), jobId));
            }

            if (companyId != null) {
                Join<JobQuestion, Job> jobJoin =
                        root.join("job");
                Join<Job, Company> companyJoin =
                        jobJoin.join("company");

                predicates.add(
                        companyJoin.get("id").in(companyId)
                );
            }

            // Filter by deleted status
            if (request.getDeleted() != null) {
                predicates.add(cb.notEqual(root.get("deleted"), request.getDeleted()));
            }

            // Filter by keyword (search in question and description)
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("question")), keyword),
                                cb.like(cb.lower(root.get("description")), keyword)));

            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
