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
import com.sma.core.entity.Criteria;
import com.sma.core.entity.EvaluationCriteriaScore;
import com.sma.core.entity.EvaluationExperienceDetail;
import com.sma.core.entity.EvaluationGap;
import com.sma.core.entity.EvaluationHardSkill;
import com.sma.core.entity.EvaluationSoftSkill;
import com.sma.core.entity.EvaluationWeakness;
import com.sma.core.entity.ExperienceSkill;
import com.sma.core.entity.Job;
import com.sma.core.entity.ProjectSkill;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeCertification;
import com.sma.core.entity.ResumeEducation;
import com.sma.core.entity.ResumeEvaluation;
import com.sma.core.entity.ResumeExperience;
import com.sma.core.entity.ResumeExperienceDetail;
import com.sma.core.entity.ResumeProject;
import com.sma.core.entity.ResumeSkill;
import com.sma.core.entity.ScoringCriteria;
import com.sma.core.entity.Skill;
import com.sma.core.entity.SkillCategory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Component
public class ResumeDetailMapper {

    public ResumeDetailResponse toDetailResponse(Resume resume) {
        ResumeDetailResponse response = new ResumeDetailResponse();
        response.setId(resume.getId());
        response.setCandidateId(resume.getCandidate() != null ? resume.getCandidate().getId() : null);
        response.setResumeName(resume.getResumeName());
        response.setFileName(resume.getFileName());
        response.setRawText(resume.getRawText());
        response.setAddressInResume(resume.getAddressInResume());
        response.setPhoneInResume(resume.getPhoneInResume());
        response.setEmailInResume(resume.getEmailInResume());
        response.setGithubLink(resume.getGithubLink());
        response.setLinkedinLink(resume.getLinkedinLink());
        response.setPortfolioLink(resume.getPortfolioLink());
        response.setFullName(resume.getFullName());
        response.setAvatar(resume.getAvatar());
        response.setResumeUrl(resume.getResumeUrl());
        response.setType(resume.getType());
        response.setRootResumeId(resume.getRootResume() != null ? resume.getRootResume().getId() : null);
        response.setRootResumeName(resume.getRootResume() != null ? resume.getRootResume().getResumeName() : null);
        response.setStatus(resume.getStatus());
        response.setParseStatus(resume.getParseStatus());
        response.setLanguage(resume.getLanguage());
        response.setIsDefault(resume.getIsDefault());
        response.setIsOverrided(resume.getIsOverrided());
        response.setSkills(mapResumeSkills(resume.getSkills()));
        response.setEducations(mapResumeEducations(resume.getEducations()));
        response.setExperiences(mapResumeExperiences(resume.getExperiences()));
        response.setProjects(mapResumeProjects(resume.getProjects()));
        response.setCertifications(mapResumeCertifications(resume.getCertifications()));
        response.setEvaluations(mapResumeEvaluations(resume.getEvaluations()));
        return response;
    }

    private List<ResumeSkillDetailResponse> mapResumeSkills(Collection<ResumeSkill> values) {
        return sortById(values, ResumeSkill::getId).stream()
                .map(this::toResumeSkillResponse)
                .toList();
    }

    private ResumeSkillDetailResponse toResumeSkillResponse(ResumeSkill item) {
        Skill skill = item.getSkill();
        SkillCategory category = skill != null ? skill.getCategory() : null;
        ResumeSkillDetailResponse response = new ResumeSkillDetailResponse();
        response.setId(item.getId());
        response.setRawSkillSection(item.getRawSkillSection());
        response.setSkillId(skill != null ? skill.getId() : null);
        response.setSkillName(skill != null ? skill.getName() : null);
        response.setSkillDescription(skill != null ? skill.getDescription() : null);
        response.setSkillCategoryId(category != null ? category.getId() : null);
        response.setSkillCategoryName(category != null ? category.getName() : null);
        return response;
    }

    private List<ResumeEducationDetailResponse> mapResumeEducations(Collection<ResumeEducation> values) {
        return sortById(values, ResumeEducation::getId).stream()
                .map(item -> {
                    ResumeEducationDetailResponse response = new ResumeEducationDetailResponse();
                    response.setId(item.getId());
                    response.setInstitution(item.getInstitution());
                    response.setDegree(item.getDegree());
                    response.setMajorField(item.getMajorField());
                    response.setGpa(item.getGpa());
                    response.setStartDate(item.getStartDate());
                    response.setEndDate(item.getEndDate());
                    response.setIsCurrent(item.getIsCurrent());
                    return response;
                })
                .toList();
    }

    private List<ResumeExperienceResponse> mapResumeExperiences(Collection<ResumeExperience> values) {
        return sortById(values, ResumeExperience::getId).stream()
                .map(this::toResumeExperienceResponse)
                .toList();
    }

    private ResumeExperienceResponse toResumeExperienceResponse(ResumeExperience item) {
        ResumeExperienceResponse response = new ResumeExperienceResponse();
        response.setId(item.getId());
        response.setCompany(item.getCompany());
        response.setStartDate(item.getStartDate());
        response.setEndDate(item.getEndDate());
        response.setIsCurrent(item.getIsCurrent());
        response.setDetails(sortById(item.getDetails(), ResumeExperienceDetail::getId).stream()
                .map(this::toResumeExperienceDetailResponse)
                .toList());
        return response;
    }

    private ResumeExperienceDetailResponse toResumeExperienceDetailResponse(ResumeExperienceDetail item) {
        ResumeExperienceDetailResponse response = new ResumeExperienceDetailResponse();
        response.setId(item.getId());
        response.setDescription(item.getDescription());
        response.setTitle(item.getTitle());
        response.setPosition(item.getPosition());
        response.setStartDate(item.getStartDate());
        response.setEndDate(item.getEndDate());
        response.setIsCurrent(item.getIsCurrent());
        response.setSkills(sortById(item.getSkills(), ExperienceSkill::getId).stream()
                .map(this::toExperienceSkillResponse)
                .toList());
        return response;
    }

    private ExperienceSkillResponse toExperienceSkillResponse(ExperienceSkill item) {
        ExperienceSkillResponse response = new ExperienceSkillResponse();
        response.setId(item.getId());
        response.setDescription(item.getDescription());
        response.setSkillId(item.getSkill() != null ? item.getSkill().getId() : null);
        response.setSkillName(item.getSkill() != null ? item.getSkill().getName() : null);
        return response;
    }

    private List<ResumeProjectResponse> mapResumeProjects(Collection<ResumeProject> values) {
        return sortById(values, ResumeProject::getId).stream()
                .map(this::toResumeProjectResponse)
                .toList();
    }

    private ResumeProjectResponse toResumeProjectResponse(ResumeProject item) {
        ResumeProjectResponse response = new ResumeProjectResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setTeamSize(item.getTeamSize());
        response.setPosition(item.getPosition());
        response.setDescription(item.getDescription());
        response.setProjectType(item.getProjectType());
        response.setStartDate(item.getStartDate());
        response.setEndDate(item.getEndDate());
        response.setIsCurrent(item.getIsCurrent());
        response.setProjectUrl(item.getProjectUrl());
        response.setSkills(sortById(item.getSkills(), ProjectSkill::getId).stream()
                .map(this::toProjectSkillResponse)
                .toList());
        return response;
    }

    private ProjectSkillResponse toProjectSkillResponse(ProjectSkill item) {
        ProjectSkillResponse response = new ProjectSkillResponse();
        response.setId(item.getId());
        response.setDescription(item.getDescription());
        response.setSkillId(item.getSkill() != null ? item.getSkill().getId() : null);
        response.setSkillName(item.getSkill() != null ? item.getSkill().getName() : null);
        return response;
    }

    private List<ResumeCertificationDetailResponse> mapResumeCertifications(Collection<ResumeCertification> values) {
        return sortById(values, ResumeCertification::getId).stream()
                .map(item -> {
                    ResumeCertificationDetailResponse response = new ResumeCertificationDetailResponse();
                    response.setId(item.getId());
                    response.setName(item.getName());
                    response.setIssuer(item.getIssuer());
                    response.setCredentialUrl(item.getCredentialUrl());
                    response.setImage(item.getImage());
                    response.setDescription(item.getDescription());
                    return response;
                })
                .toList();
    }

    private List<ResumeEvaluationResponse> mapResumeEvaluations(Collection<ResumeEvaluation> values) {
        return sortById(values, ResumeEvaluation::getId).stream()
                .map(this::toResumeEvaluationResponse)
                .toList();
    }

    private ResumeEvaluationResponse toResumeEvaluationResponse(ResumeEvaluation item) {
        Job job = item.getJob();
        ResumeEvaluationResponse response = new ResumeEvaluationResponse();
        response.setId(item.getId());
        response.setAiOverallScore(item.getAiOverallScore());
        response.setRecruiterOverallScore(item.getRecruiterOverallScore());
        response.setMatchLevel(item.getMatchLevel());
        response.setSummary(item.getSummary());
        response.setStrengths(item.getStrengths());
        response.setWeakness(item.getWeakness());
        response.setIsTrueLevel(item.getIsTrueLevel());
        response.setHasRelatedExperience(item.getHasRelatedExperience());
        response.setIsSpecificJd(item.getIsSpecificJd());
        response.setEvaluationStatus(item.getEvaluationStatus());
        response.setProcessingTimeSecond(item.getProcessingTimeSecond());
        response.setAiModelVersion(item.getAiModelVersion());
        response.setJobId(job != null ? job.getId() : null);
        response.setJobName(job != null ? job.getName() : null);
        response.setCriteriaScores(sortById(item.getCriteriaScores(), EvaluationCriteriaScore::getId).stream()
                .map(this::toEvaluationCriteriaScoreResponse)
                .toList());
        response.setGaps(sortById(item.getGaps(), EvaluationGap::getId).stream()
                .map(this::toEvaluationGapResponse)
                .toList());
        response.setWeaknesses(sortById(item.getWeaknesses(), EvaluationWeakness::getId).stream()
                .map(this::toEvaluationWeaknessResponse)
                .toList());
        return response;
    }

    private EvaluationCriteriaScoreResponse toEvaluationCriteriaScoreResponse(EvaluationCriteriaScore item) {
        ScoringCriteria scoringCriteria = item.getScoringCriteria();
        Criteria criteria = scoringCriteria != null ? scoringCriteria.getCriteria() : null;

        EvaluationCriteriaScoreResponse response = new EvaluationCriteriaScoreResponse();
        response.setId(item.getId());
        response.setScoringCriteriaId(scoringCriteria != null ? scoringCriteria.getId() : null);
        response.setScoringCriteriaContext(scoringCriteria != null ? scoringCriteria.getContext() : null);
        response.setScoringCriteriaWeight(scoringCriteria != null ? scoringCriteria.getWeight() : null);
        response.setCriteriaId(criteria != null ? criteria.getId() : null);
        response.setCriteriaName(criteria != null ? criteria.getName() : null);
        response.setCriteriaType(criteria != null ? criteria.getCriteriaType() : null);
        response.setMaxScore(item.getMaxScore());
        response.setAiScore(item.getAiScore());
        response.setManualScore(item.getManualScore());
        response.setWeightedScore(item.getWeightedScore());
        response.setAiExplanation(item.getAiExplanation());
        response.setManualExplanation(item.getManualExplanation());
        response.setHardSkills(sortById(item.getHardSkills(), EvaluationHardSkill::getId).stream()
                .map(this::toEvaluationHardSkillResponse)
                .toList());
        response.setSoftSkills(sortById(item.getSoftSkills(), EvaluationSoftSkill::getId).stream()
                .map(this::toEvaluationSoftSkillResponse)
                .toList());
        response.setExperienceDetails(sortById(item.getExperienceDetails(), EvaluationExperienceDetail::getId).stream()
                .map(this::toEvaluationExperienceDetailResponse)
                .toList());
        return response;
    }

    private EvaluationHardSkillResponse toEvaluationHardSkillResponse(EvaluationHardSkill item) {
        EvaluationHardSkillResponse response = new EvaluationHardSkillResponse();
        response.setId(item.getId());
        response.setSkillName(item.getSkillName());
        response.setEvidence(item.getEvidence());
        response.setSkillCategory(item.getSkillCategory());
        response.setRequiredLevel(item.getRequiredLevel());
        response.setCandidateLevel(item.getCandidateLevel());
        response.setMatchScore(item.getMatchScore());
        response.setYearsOfExperience(item.getYearsOfExperience());
        response.setIsCritical(item.getIsCritical());
        response.setIsMatched(item.getIsMatched());
        response.setIsMissing(item.getIsMissing());
        response.setIsExtra(item.getIsExtra());
        response.setRelevance(item.getRelevance());
        return response;
    }

    private EvaluationSoftSkillResponse toEvaluationSoftSkillResponse(EvaluationSoftSkill item) {
        EvaluationSoftSkillResponse response = new EvaluationSoftSkillResponse();
        response.setId(item.getId());
        response.setSkillName(item.getSkillName());
        response.setEvidence(item.getEvidence());
        response.setIsRequired(item.getIsRequired());
        response.setIsFound(item.getIsFound());
        return response;
    }

    private EvaluationExperienceDetailResponse toEvaluationExperienceDetailResponse(EvaluationExperienceDetail item) {
        EvaluationExperienceDetailResponse response = new EvaluationExperienceDetailResponse();
        response.setId(item.getId());
        response.setCompanyName(item.getCompanyName());
        response.setPosition(item.getPosition());
        response.setDurationMonths(item.getDurationMonths());
        response.setKeyAchievements(item.getKeyAchievements());
        response.setTechnologiesUsed(item.getTechnologiesUsed());
        response.setIsRelevant(item.getIsRelevant());
        response.setTransferabilityToRole(item.getTransferabilityToRole());
        response.setExperienceGravity(item.getExperienceGravity());
        return response;
    }

    private EvaluationGapResponse toEvaluationGapResponse(EvaluationGap item) {
        EvaluationGapResponse response = new EvaluationGapResponse();
        response.setId(item.getId());
        response.setGapType(item.getGapType());
        response.setItemName(item.getItemName());
        response.setDescription(item.getDescription());
        response.setImpact(item.getImpact());
        response.setImpactScore(item.getImpactScore());
        response.setSuggestion(item.getSuggestion());
        return response;
    }

    private EvaluationWeaknessResponse toEvaluationWeaknessResponse(EvaluationWeakness item) {
        EvaluationWeaknessResponse response = new EvaluationWeaknessResponse();
        response.setId(item.getId());
        response.setWeaknessText(item.getWeaknessText());
        response.setSuggestion(item.getSuggestion());
        response.setStartIndex(item.getStartIndex());
        response.setEndIndex(item.getEndIndex());
        response.setContext(item.getContext());
        response.setCriterionType(item.getCriterionType());
        response.setSeverity(item.getSeverity());
        return response;
    }

    private <T> List<T> sortById(Collection<T> values, Function<T, Integer> idExtractor) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .sorted(Comparator.comparing(idExtractor, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }
}
