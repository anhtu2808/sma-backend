package com.sma.core.service.impl;

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
import com.sma.core.mapper.resume.ResumeCloneMapper;
import com.sma.core.service.ResumeCloneService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResumeCloneServiceImpl implements ResumeCloneService {
    ResumeCloneMapper resumeCloneMapper;

    @Override
    public void cloneAll(Resume source, Resume target) {
        cloneSkillGroups(source, target);
        cloneEducations(source, target);
        cloneExperiences(source, target);
        cloneProjects(source, target);
        cloneCertifications(source, target);
    }

    @Override
    public void cloneSkillGroups(Resume source, Resume target) {
        if (source.getSkillGroups() == null) {
            return;
        }
        for (ResumeSkillGroup group : source.getSkillGroups()) {
            ResumeSkillGroup groupClone = resumeCloneMapper.cloneSkillGroup(group, target);

            if (group.getSkills() != null) {
                for (ResumeSkill skill : group.getSkills()) {
                    ResumeSkill skillClone = resumeCloneMapper.cloneResumeSkill(skill, groupClone);
                    groupClone.getSkills().add(skillClone);
                }
            }

            target.getSkillGroups().add(groupClone);
        }
    }

    @Override
    public void cloneEducations(Resume source, Resume target) {
        if (source.getEducations() == null) {
            return;
        }
        for (ResumeEducation education : source.getEducations()) {
            ResumeEducation educationClone = resumeCloneMapper.cloneResumeEducation(education, target);
            target.getEducations().add(educationClone);
        }
    }

    @Override
    public void cloneExperiences(Resume source, Resume target) {
        if (source.getExperiences() == null) {
            return;
        }
        for (ResumeExperience experience : source.getExperiences()) {
            ResumeExperience experienceClone = resumeCloneMapper.cloneResumeExperience(experience, target);

            if (experience.getDetails() != null) {
                for (ResumeExperienceDetail detail : experience.getDetails()) {
                    ResumeExperienceDetail detailClone =
                            resumeCloneMapper.cloneResumeExperienceDetail(detail, experienceClone);

                    if (detail.getSkills() != null) {
                        for (ExperienceSkill skill : detail.getSkills()) {
                            ExperienceSkill skillClone = resumeCloneMapper.cloneExperienceSkill(skill, detailClone);
                            detailClone.getSkills().add(skillClone);
                        }
                    }

                    experienceClone.getDetails().add(detailClone);
                }
            }

            target.getExperiences().add(experienceClone);
        }
    }

    @Override
    public void cloneProjects(Resume source, Resume target) {
        if (source.getProjects() == null) {
            return;
        }
        for (ResumeProject project : source.getProjects()) {
            ResumeProject projectClone = resumeCloneMapper.cloneResumeProject(project, target);

            if (project.getSkills() != null) {
                for (ProjectSkill skill : project.getSkills()) {
                    ProjectSkill skillClone = resumeCloneMapper.cloneProjectSkill(skill, projectClone);
                    projectClone.getSkills().add(skillClone);
                }
            }

            target.getProjects().add(projectClone);
        }
    }

    @Override
    public void cloneCertifications(Resume source, Resume target) {
        if (source.getCertifications() == null) {
            return;
        }
        for (ResumeCertification certification : source.getCertifications()) {
            ResumeCertification certificationClone = resumeCloneMapper.cloneResumeCertification(certification, target);
            target.getCertifications().add(certificationClone);
        }
    }
}
