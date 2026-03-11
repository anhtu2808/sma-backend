package com.sma.core.mapper.evaluation;

import com.sma.core.dto.message.matching.MatchingRequestMessage;
import com.sma.core.dto.request.evaluation.*;
import com.sma.core.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface MatchingRequestMapper {

    // ---- Experience mapping ----

    @Mapping(target = "description", source = "company")
    @Mapping(target = "details", source = "details")
    ResumeExperienceRequest toExperienceRequest(ResumeExperience experience);

    ResumeExperienceDetailRequest toExperienceDetailRequest(ResumeExperienceDetail detail);

    @Mapping(target = "description", source = "description")
    ResumeExperienceSkillRequest toExperienceSkillRequest(ExperienceSkill experienceSkill);

    default Set<ResumeExperienceRequest> toExperienceRequests(Set<ResumeExperience> experiences) {
        if (experiences == null) return new HashSet<>();
        return experiences.stream().map(this::toExperienceRequest).collect(Collectors.toSet());
    }

    // ---- Project mapping ----

    @Mapping(target = "skills", source = "skills")
    ResumeProjectRequest toProjectRequest(ResumeProject project);

    @Mapping(target = "description", source = "description")
    ResumeExperienceSkillRequest toProjectSkillRequest(ProjectSkill projectSkill);

    default Set<ResumeExperienceSkillRequest> mapProjectSkills(Set<ProjectSkill> skills) {
        if (skills == null) return new HashSet<>();
        return skills.stream().map(this::toProjectSkillRequest).collect(Collectors.toSet());
    }

    default Set<ResumeProjectRequest> toProjectRequests(Set<ResumeProject> projects) {
        if (projects == null) return new HashSet<>();
        return projects.stream().map(this::toProjectRequest).collect(Collectors.toSet());
    }

    // ---- Education mapping ----

    ResumeEducationRequest toEducationRequest(ResumeEducation education);

    default Set<ResumeEducationRequest> toEducationRequests(Set<ResumeEducation> educations) {
        if (educations == null) return new HashSet<>();
        return educations.stream().map(this::toEducationRequest).collect(Collectors.toSet());
    }

    // ---- Scoring Criteria mapping ----

    @Mapping(target = "criteriaType", source = "criteria.criteriaType")
    JobScoringCriteriaRequest toCriteriaRequest(ScoringCriteria scoringCriteria);

    default Set<JobScoringCriteriaRequest> toCriteriaRequests(Set<ScoringCriteria> criterias) {
        if (criterias == null) return new HashSet<>();
        return criterias.stream().map(this::toCriteriaRequest).collect(Collectors.toSet());
    }

    // ---- Skill mapping (manual due to group logic) ----

    default Set<ResumeHardSkillRequest> toHardSkillRequests(Set<ResumeSkillGroup> skillGroups) {
        Set<ResumeHardSkillRequest> result = new HashSet<>();
        if (skillGroups == null) return result;
        skillGroups.forEach(group -> {
            String groupName = group.getName() != null ? group.getName().toLowerCase() : "";
            if (groupName.contains("soft")) return;
            group.getSkills().forEach(resumeSkill -> {
                if (resumeSkill.getSkill() == null || resumeSkill.getSkill().getName() == null) return;
                result.add(ResumeHardSkillRequest.builder()
                        .name(resumeSkill.getSkill().getName())
                        .yearsOfExperience(resumeSkill.getYearsOfExperience())
                        .build());
            });
        });
        return result;
    }

    default Set<ResumeSoftSkillRequest> toSoftSkillRequests(Set<ResumeSkillGroup> skillGroups) {
        Set<ResumeSoftSkillRequest> result = new HashSet<>();
        if (skillGroups == null) return result;
        skillGroups.forEach(group -> {
            String groupName = group.getName() != null ? group.getName().toLowerCase() : "";
            if (!groupName.contains("soft")) return;
            group.getSkills().forEach(resumeSkill -> {
                if (resumeSkill.getSkill() == null || resumeSkill.getSkill().getName() == null) return;
                result.add(ResumeSoftSkillRequest.builder()
                        .name(resumeSkill.getSkill().getName())
                        .build());
            });
        });
        return result;
    }

    // ---- Build complete message ----

    default MatchingRequestMessage buildMessage(ResumeEvaluation evaluation, Resume resume, Job job) {
        return MatchingRequestMessage.builder()
                .evaluationId(evaluation.getId())
                .resumeId(resume.getId())
                .jobId(job.getId())
                .rawResumeText(resume.getRawText())
                .resumeName(resume.getResumeName())
                .resumeFileName(resume.getFileName())
                .candidateFullName(resume.getFullName())
                .jobTitle(job.getName())
                .criteria(toCriteriaRequests(job.getScoringCriterias()))
                .build();
    }
}
