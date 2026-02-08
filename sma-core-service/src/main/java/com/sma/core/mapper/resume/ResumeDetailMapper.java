package com.sma.core.mapper.resume;

import com.sma.core.dto.response.resume.EvaluationCriteriaScoreResponse;
import com.sma.core.dto.response.resume.EvaluationExperienceDetailResponse;
import com.sma.core.dto.response.resume.EvaluationGapResponse;
import com.sma.core.dto.response.resume.EvaluationHardSkillResponse;
import com.sma.core.dto.response.resume.EvaluationSoftSkillResponse;
import com.sma.core.dto.response.resume.EvaluationWeaknessResponse;
import com.sma.core.dto.response.resume.ExperienceSkillResponse;
import com.sma.core.dto.response.resume.ProjectSkillResponse;
import com.sma.core.dto.response.resume.ResumeCertificationDetailResponse;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.resume.ResumeEducationDetailResponse;
import com.sma.core.dto.response.resume.ResumeEvaluationResponse;
import com.sma.core.dto.response.resume.ResumeExperienceDetailResponse;
import com.sma.core.dto.response.resume.ResumeExperienceResponse;
import com.sma.core.dto.response.resume.ResumeProjectResponse;
import com.sma.core.dto.response.resume.ResumeSkillDetailResponse;
import com.sma.core.entity.EvaluationCriteriaScore;
import com.sma.core.entity.EvaluationExperienceDetail;
import com.sma.core.entity.EvaluationGap;
import com.sma.core.entity.EvaluationHardSkill;
import com.sma.core.entity.EvaluationSoftSkill;
import com.sma.core.entity.EvaluationWeakness;
import com.sma.core.entity.ExperienceSkill;
import com.sma.core.entity.ProjectSkill;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeCertification;
import com.sma.core.entity.ResumeEducation;
import com.sma.core.entity.ResumeEvaluation;
import com.sma.core.entity.ResumeExperience;
import com.sma.core.entity.ResumeExperienceDetail;
import com.sma.core.entity.ResumeProject;
import com.sma.core.entity.ResumeSkill;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Mapper(componentModel = "spring")
public interface ResumeDetailMapper {

    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "rootResumeId", source = "rootResume.id")
    @Mapping(target = "rootResumeName", source = "rootResume.resumeName")
    ResumeDetailResponse toDetailResponse(Resume resume);

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    @Mapping(target = "skillDescription", source = "skill.description")
    @Mapping(target = "skillCategoryId", source = "skill.category.id")
    @Mapping(target = "skillCategoryName", source = "skill.category.name")
    ResumeSkillDetailResponse toResumeSkillDetailResponse(ResumeSkill item);

    ResumeEducationDetailResponse toResumeEducationDetailResponse(ResumeEducation item);

    ResumeExperienceResponse toResumeExperienceResponse(ResumeExperience item);

    ResumeExperienceDetailResponse toResumeExperienceDetailResponse(ResumeExperienceDetail item);

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    ExperienceSkillResponse toExperienceSkillResponse(ExperienceSkill item);

    ResumeProjectResponse toResumeProjectResponse(ResumeProject item);

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    ProjectSkillResponse toProjectSkillResponse(ProjectSkill item);

    ResumeCertificationDetailResponse toResumeCertificationDetailResponse(ResumeCertification item);

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobName", source = "job.name")
    ResumeEvaluationResponse toResumeEvaluationResponse(ResumeEvaluation item);

    @Mapping(target = "scoringCriteriaId", source = "scoringCriteria.id")
    @Mapping(target = "scoringCriteriaContext", source = "scoringCriteria.context")
    @Mapping(target = "scoringCriteriaWeight", source = "scoringCriteria.weight")
    @Mapping(target = "criteriaId", source = "scoringCriteria.criteria.id")
    @Mapping(target = "criteriaName", source = "scoringCriteria.criteria.name")
    @Mapping(target = "criteriaType", source = "scoringCriteria.criteria.criteriaType")
    EvaluationCriteriaScoreResponse toEvaluationCriteriaScoreResponse(EvaluationCriteriaScore item);

    EvaluationHardSkillResponse toEvaluationHardSkillResponse(EvaluationHardSkill item);

    EvaluationSoftSkillResponse toEvaluationSoftSkillResponse(EvaluationSoftSkill item);

    EvaluationExperienceDetailResponse toEvaluationExperienceDetailResponse(EvaluationExperienceDetail item);

    EvaluationGapResponse toEvaluationGapResponse(EvaluationGap item);

    EvaluationWeaknessResponse toEvaluationWeaknessResponse(EvaluationWeakness item);

    @AfterMapping
    default void sortResumeDetailCollections(@MappingTarget ResumeDetailResponse response) {
        response.setSkills(sortById(response.getSkills(), ResumeSkillDetailResponse::getId));
        response.setEducations(sortById(response.getEducations(), ResumeEducationDetailResponse::getId));
        response.setExperiences(sortById(response.getExperiences(), ResumeExperienceResponse::getId));
        response.setProjects(sortById(response.getProjects(), ResumeProjectResponse::getId));
        response.setCertifications(sortById(response.getCertifications(), ResumeCertificationDetailResponse::getId));
        response.setEvaluations(sortById(response.getEvaluations(), ResumeEvaluationResponse::getId));
    }

    @AfterMapping
    default void sortResumeExperienceCollections(@MappingTarget ResumeExperienceResponse response) {
        response.setDetails(sortById(response.getDetails(), ResumeExperienceDetailResponse::getId));
    }

    @AfterMapping
    default void sortResumeExperienceDetailCollections(@MappingTarget ResumeExperienceDetailResponse response) {
        response.setSkills(sortById(response.getSkills(), ExperienceSkillResponse::getId));
    }

    @AfterMapping
    default void sortResumeProjectCollections(@MappingTarget ResumeProjectResponse response) {
        response.setSkills(sortById(response.getSkills(), ProjectSkillResponse::getId));
    }

    @AfterMapping
    default void sortResumeEvaluationCollections(@MappingTarget ResumeEvaluationResponse response) {
        response.setCriteriaScores(sortById(response.getCriteriaScores(), EvaluationCriteriaScoreResponse::getId));
        response.setGaps(sortById(response.getGaps(), EvaluationGapResponse::getId));
        response.setWeaknesses(sortById(response.getWeaknesses(), EvaluationWeaknessResponse::getId));
    }

    @AfterMapping
    default void sortEvaluationCriteriaScoreCollections(@MappingTarget EvaluationCriteriaScoreResponse response) {
        response.setHardSkills(sortById(response.getHardSkills(), EvaluationHardSkillResponse::getId));
        response.setSoftSkills(sortById(response.getSoftSkills(), EvaluationSoftSkillResponse::getId));
        response.setExperienceDetails(sortById(response.getExperienceDetails(), EvaluationExperienceDetailResponse::getId));
    }

    private <T> List<T> sortById(List<T> values, Function<T, Integer> idExtractor) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .sorted(Comparator.comparing(idExtractor, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }
}
