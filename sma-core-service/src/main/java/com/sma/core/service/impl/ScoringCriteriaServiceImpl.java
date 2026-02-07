package com.sma.core.service.impl;

import com.sma.core.dto.request.job.AddJobScoringCriteriaRequest;
import com.sma.core.entity.Criteria;
import com.sma.core.entity.Job;
import com.sma.core.entity.ScoringCriteria;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.repository.CriteriaRepository;
import com.sma.core.repository.ScoringCriteriaRepository;
import com.sma.core.service.ScoringCriteriaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ScoringCriteriaServiceImpl implements ScoringCriteriaService {

    final CriteriaRepository criteriaRepository;
    final ScoringCriteriaRepository scoringCriteriaRepository;

    @Override
    public Set<ScoringCriteria> saveJobScoringCriteria(
            Job job,
            Set<AddJobScoringCriteriaRequest> requests) {

        Set<ScoringCriteria> result = new HashSet<>();

        double totalWeight = 0.0;

        for (AddJobScoringCriteriaRequest request : requests) {
            Criteria criteria = criteriaRepository.findById(request.getCriteriaId())
                    .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_NOT_EXISTED));
            boolean enable = Boolean.TRUE.equals(request.getEnable());
            ScoringCriteria existing = job == null
                    ? null
                    : scoringCriteriaRepository
                    .findByCriteria_IdAndJob_Id(criteria.getId(), job.getId())
                    .orElse(null);
            if (!enable) {
                if (existing != null) {
                    scoringCriteriaRepository.delete(existing);
                }
                continue;
            }
            double weight = request.getWeight() != null
                    ? request.getWeight()
                    : criteria.getDefaultWeight();

            totalWeight += weight;
            if (existing == null) {

                ScoringCriteria sc = ScoringCriteria.builder()
                        .criteria(criteria)
                        .job(job)
                        .weight(weight)
                        .build();

                result.add(sc);

            } else {
                existing.setWeight(weight);
                result.add(existing);
            }
        }

        if (Math.abs(totalWeight - 100.0) > 0.001) {
            throw new AppException(ErrorCode.INVALID_TOTAL_WEIGHT);
        }

        return result;
    }
}
