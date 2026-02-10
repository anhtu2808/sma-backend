package com.sma.core.mapper.resume;

import com.sma.core.entity.ExperienceSkill;
import com.sma.core.entity.ProjectSkill;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeCertification;
import com.sma.core.entity.ResumeEducation;
import com.sma.core.entity.ResumeExperience;
import com.sma.core.entity.ResumeExperienceDetail;
import com.sma.core.entity.ResumeProject;
import com.sma.core.entity.ResumeSkill;
import com.sma.core.entity.ResumeSkillGroup;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResumeCloneMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "source.name")
    @Mapping(target = "orderIndex", source = "source.orderIndex")
    @Mapping(target = "resume", source = "targetResume")
    @Mapping(target = "skills", ignore = true)
    ResumeSkillGroup cloneSkillGroup(ResumeSkillGroup source, Resume targetResume);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "yearsOfExperience", source = "source.yearsOfExperience")
    @Mapping(target = "skill", source = "source.skill")
    @Mapping(target = "skillGroup", source = "targetGroup")
    ResumeSkill cloneResumeSkill(ResumeSkill source, ResumeSkillGroup targetGroup);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "institution", source = "source.institution")
    @Mapping(target = "degree", source = "source.degree")
    @Mapping(target = "majorField", source = "source.majorField")
    @Mapping(target = "gpa", source = "source.gpa")
    @Mapping(target = "startDate", source = "source.startDate")
    @Mapping(target = "endDate", source = "source.endDate")
    @Mapping(target = "isCurrent", source = "source.isCurrent")
    @Mapping(target = "orderIndex", source = "source.orderIndex")
    @Mapping(target = "resume", source = "targetResume")
    ResumeEducation cloneResumeEducation(ResumeEducation source, Resume targetResume);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", source = "source.company")
    @Mapping(target = "startDate", source = "source.startDate")
    @Mapping(target = "endDate", source = "source.endDate")
    @Mapping(target = "isCurrent", source = "source.isCurrent")
    @Mapping(target = "workingModel", source = "source.workingModel")
    @Mapping(target = "employmentType", source = "source.employmentType")
    @Mapping(target = "orderIndex", source = "source.orderIndex")
    @Mapping(target = "resume", source = "targetResume")
    @Mapping(target = "details", ignore = true)
    ResumeExperience cloneResumeExperience(ResumeExperience source, Resume targetResume);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "source.description")
    @Mapping(target = "title", source = "source.title")
    @Mapping(target = "startDate", source = "source.startDate")
    @Mapping(target = "endDate", source = "source.endDate")
    @Mapping(target = "isCurrent", source = "source.isCurrent")
    @Mapping(target = "orderIndex", source = "source.orderIndex")
    @Mapping(target = "experience", source = "targetExperience")
    @Mapping(target = "skills", ignore = true)
    ResumeExperienceDetail cloneResumeExperienceDetail(ResumeExperienceDetail source, ResumeExperience targetExperience);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "source.description")
    @Mapping(target = "skill", source = "source.skill")
    @Mapping(target = "detail", source = "targetDetail")
    ExperienceSkill cloneExperienceSkill(ExperienceSkill source, ResumeExperienceDetail targetDetail);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "title", source = "source.title")
    @Mapping(target = "teamSize", source = "source.teamSize")
    @Mapping(target = "position", source = "source.position")
    @Mapping(target = "description", source = "source.description")
    @Mapping(target = "projectType", source = "source.projectType")
    @Mapping(target = "startDate", source = "source.startDate")
    @Mapping(target = "endDate", source = "source.endDate")
    @Mapping(target = "isCurrent", source = "source.isCurrent")
    @Mapping(target = "projectUrl", source = "source.projectUrl")
    @Mapping(target = "orderIndex", source = "source.orderIndex")
    @Mapping(target = "resume", source = "targetResume")
    @Mapping(target = "skills", ignore = true)
    ResumeProject cloneResumeProject(ResumeProject source, Resume targetResume);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", source = "source.description")
    @Mapping(target = "skill", source = "source.skill")
    @Mapping(target = "project", source = "targetProject")
    ProjectSkill cloneProjectSkill(ProjectSkill source, ResumeProject targetProject);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "source.name")
    @Mapping(target = "issuer", source = "source.issuer")
    @Mapping(target = "credentialUrl", source = "source.credentialUrl")
    @Mapping(target = "image", source = "source.image")
    @Mapping(target = "description", source = "source.description")
    @Mapping(target = "resume", source = "targetResume")
    ResumeCertification cloneResumeCertification(ResumeCertification source, Resume targetResume);
}
