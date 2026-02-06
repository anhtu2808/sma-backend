package com.sma.core.service;

import com.sma.core.dto.request.job.AddJobScoringCriteriaRequest;
import com.sma.core.entity.Job;
import com.sma.core.entity.ScoringCriteria;

import java.util.Set;

public interface ScoringCriteriaService {

    Set<ScoringCriteria> saveJobScoringCriteria(Job job, Set<AddJobScoringCriteriaRequest> requests);

}
