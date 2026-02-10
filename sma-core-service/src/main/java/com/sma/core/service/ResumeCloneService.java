package com.sma.core.service;

import com.sma.core.entity.Resume;

public interface ResumeCloneService {
    void cloneAll(Resume source, Resume target);
    void cloneSkillGroups(Resume source, Resume target);
    void cloneEducations(Resume source, Resume target);
    void cloneExperiences(Resume source, Resume target);
    void cloneProjects(Resume source, Resume target);
    void cloneCertifications(Resume source, Resume target);
}
