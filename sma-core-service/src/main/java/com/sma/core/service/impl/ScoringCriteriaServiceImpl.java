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
    Double totalWeight = 0.0;

    @Override
    public Set<ScoringCriteria> saveJobScoringCriteria(Set<AddJobScoringCriteriaRequest> requests) {
        Set<ScoringCriteria> scoringCriteria = new HashSet<>();
        for (AddJobScoringCriteriaRequest criteriaRequest : requests) {
            Criteria criteria = criteriaRepository.findById(criteriaRequest.getCriteriaId())
                    .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_NOT_EXISTED));
            if (criteriaRequest.getEnable()) {
                totalWeight += criteriaRequest.getWeight();
                scoringCriteria.add(ScoringCriteria.builder()
                        .criteria(criteria)
                        .weight(criteriaRequest.getWeight() != null
                                ? criteriaRequest.getWeight()
                                : criteria.getDefaultWeight())
                        .build());
            }
        }
        if (Math.abs(totalWeight - 100.0) > 0.001)
            throw new AppException(ErrorCode.INVALID_TOTAL_WEIGHT);
        return scoringCriteria;
    }

    @Override
    public Set<ScoringCriteria> saveJobScoringCriteria(Job job, Set<AddJobScoringCriteriaRequest> requests) {
        Set<ScoringCriteria> scoringCriteria = new HashSet<>();
        Set<ScoringCriteria> newScoringCriteria = new HashSet<>();
        requests
                .forEach(request -> {
                    ScoringCriteria jobScoringCriteria = scoringCriteriaRepository
                            .findByCriteria_IdAndJob_Id(request.getCriteriaId(), job.getId()).orElse(null);
                    if (jobScoringCriteria == null) {
                        if (request.getEnable()) {
                            totalWeight += request.getWeight();
                            ScoringCriteria newJobScoringCriteria = ScoringCriteria.builder()
                                    .weight(request.getWeight())
                                    .job(job)
                                    .criteria(criteriaRepository.getReferenceById(request.getCriteriaId()))
                                    .build();
                            newScoringCriteria.add(newJobScoringCriteria);
                            scoringCriteria.add(newJobScoringCriteria);
                        }
                    } else {
                        if (!request.getEnable()) {
                            scoringCriteriaRepository.delete(jobScoringCriteria);
                        } else {
                            totalWeight += request.getWeight();
                            jobScoringCriteria.setWeight(request.getWeight());
                            scoringCriteria.add(jobScoringCriteria);
                        }
                    }
                });
        if (Math.abs(totalWeight - 100.0) > 0.001)
            throw new AppException(ErrorCode.INVALID_TOTAL_WEIGHT);
        if (!newScoringCriteria.isEmpty())
            scoringCriteriaRepository.saveAll(newScoringCriteria);
        return scoringCriteria;
    }
}
