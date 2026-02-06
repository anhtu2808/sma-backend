package com.sma.core.mapper.job;

import com.sma.core.dto.request.job.AddJobScoringCriteriaRequest;
import com.sma.core.dto.response.job.JobScoringCriteriaResponse;
import com.sma.core.entity.ScoringCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ScoringCriteriaMapper {

    ScoringCriteria updateScoringCriteria(AddJobScoringCriteriaRequest request, @MappingTarget ScoringCriteria scoringCriteria);
    JobScoringCriteriaResponse toJobScoringCriteriaResponse(ScoringCriteria scoringCriteria);
}
