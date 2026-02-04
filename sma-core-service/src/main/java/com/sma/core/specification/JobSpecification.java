package com.sma.core.specification;

import com.sma.core.dto.request.job.JobSearchRequest;
import com.sma.core.entity.Domain;
import com.sma.core.entity.Job;
import com.sma.core.entity.Skill;
import com.sma.core.enums.JobStatus;
import com.sma.core.enums.Role;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> withFilter(
            JobSearchRequest request,
            EnumSet<JobStatus> allowedStatus,
            Integer companyId,
            Role role) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role == Role.ADMIN || role == Role.RECRUITER) {
                if (companyId != null) {
                    predicates.add(cb.equal(root.get("company"), companyId));
                }
            }


            // 1. Keyword search (Name, About, Responsibilities, Requirement)
            if (StringUtils.hasText(request.getName())) {
                String pattern = "%" + request.getName().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("name")), pattern),
                                cb.like(cb.lower(root.get("about")), pattern)));
            }

            // 2. Salary Range
            if (request.getSalaryStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryStart"), request.getSalaryStart()));
            }
            if (request.getSalaryEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryEnd"), request.getSalaryEnd()));
            }

            // 3. Experience Time
            if (request.getMinExperienceTime() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experienceTime"), request.getMinExperienceTime()));
            }
            if (request.getMaxExperienceTime() > 0) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceTime"), request.getMaxExperienceTime()));
            }

            // 4. Job Level
            if (request.getJobLevel() != null) {
                predicates.add(cb.equal(root.get("jobLevel"), request.getJobLevel()));
            }

            // 5. Working Model
            if (request.getWorkingModel() != null) {
                predicates.add(cb.equal(root.get("workingModel"), request.getWorkingModel()));
            }

            // 6. Skills (ManyToMany)
            if (!CollectionUtils.isEmpty(request.getSkillId())) {
                Join<Job, Skill> skillsJoin = root.join("skills");
                predicates.add(skillsJoin.get("id").in(request.getSkillId()));
                assert query != null;
                query.distinct(true);
            }

            // 7. Expertise (ManyToOne)
            if (!CollectionUtils.isEmpty(request.getExpertiseId())) {
                predicates.add(root.get("expertise").get("id").in(request.getExpertiseId()));
            }

            // 8. Domain (ManyToMany)
            if (!CollectionUtils.isEmpty(request.getDomainId())) {
                Join<Job, Domain> domainsJoin = root.join("domains");
                predicates.add(domainsJoin.get("id").in(request.getDomainId()));
                assert query != null;
                query.distinct(true);
            }

            // 9. Status (Hardcoded Approved)
            if (!allowedStatus.isEmpty())
                predicates.add(root.get("status").in(allowedStatus));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
