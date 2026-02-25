package com.sma.core.mapper.job;

import com.sma.core.dto.request.job.AdminJobSampleRequest;
import com.sma.core.dto.request.job.DraftJobRequest;
import com.sma.core.dto.request.job.PublishJobRequest;
import com.sma.core.dto.response.job.BaseJobResponse;
import com.sma.core.dto.response.job.JobDetailResponse;
import com.sma.core.entity.Job;
import com.sma.core.entity.JobQuestion;
import com.sma.core.mapper.SkillMapper;
import com.sma.core.mapper.company.CompanyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring",
        uses = {
                CompanyMapper.class,
                DomainMapper.class,
                SkillMapper.class,
                BenefitMapper.class,
                JobExpertiseMapper.class,
                ScoringCriteriaMapper.class,
                JobQuestionMapper.class
        })
public interface JobMapper {

    @Named("baseJob")
    BaseJobResponse toBaseJobResponse(Job job);

    @Mapping(target = "rootJob", ignore = true)
    @Mapping(target = "isViolated", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "autoRejectThreshold", ignore = true)
    @Mapping(target = "scoringCriterias", ignore = true)
    @Mapping(target = "enableAiScoring", ignore = true)
    @Named("clientJobDetail")
    JobDetailResponse toJobDetailResponse(Job job);

    @Named("fullJobDetail")
    JobDetailResponse toJobInternalResponse(Job job);

    @Named("publishJob")
    Job toJob(PublishJobRequest request);

    @Mapping(target = "enableAiScoring", source = "enableAiScoring")
    @Mapping(target = "autoRejectThreshold", source = "autoRejectThreshold")
    @Named("draftJob")
    Job toJob(DraftJobRequest request);

    @Named("publishExistingJob")
    Job toJob(PublishJobRequest request, @MappingTarget Job job);

    @Mapping(target = "enableAiScoring", source = "enableAiScoring")
    @Mapping(target = "autoRejectThreshold", source = "autoRejectThreshold")
    Job toJob(DraftJobRequest request, @MappingTarget Job job);

    default Job toJob(AdminJobSampleRequest request) {
        return toJob((DraftJobRequest) request);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "enableAiScoring", source = "enableAiScoring")
    @Mapping(target = "autoRejectThreshold", source = "autoRejectThreshold")
    void updateJobFromRequest(AdminJobSampleRequest request, @MappingTarget Job job);
}
