package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.ExperienceSkillRequest;
import com.sma.core.dto.response.resume.ExperienceSkillResponse;
import com.sma.core.entity.ExperienceSkill;
import com.sma.core.entity.ResumeExperienceDetail;
import com.sma.core.entity.Skill;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ExperienceSkillMapper;
import com.sma.core.repository.ExperienceSkillRepository;
import com.sma.core.repository.ResumeExperienceDetailRepository;
import com.sma.core.repository.SkillRepository;
import com.sma.core.service.ExperienceSkillService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ExperienceSkillServiceImpl implements ExperienceSkillService {

    ResumeExperienceDetailRepository resumeExperienceDetailRepository;
    ExperienceSkillRepository experienceSkillRepository;
    SkillRepository skillRepository;
    ExperienceSkillMapper experienceSkillMapper;

    @Override
    public ExperienceSkillResponse create(Integer resumeId, Integer experienceDetailId, ExperienceSkillRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeExperienceDetail detail = resumeExperienceDetailRepository
                .findByIdAndExperience_Resume_IdAndExperience_Resume_Candidate_Id(experienceDetailId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        ExperienceSkill experienceSkill = new ExperienceSkill();
        experienceSkill.setDetail(detail);

        return save(experienceSkill, request);
    }

    @Override
    public ExperienceSkillResponse update(Integer resumeId, Integer experienceSkillId, ExperienceSkillRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ExperienceSkill experienceSkill = experienceSkillRepository
                .findByIdAndDetail_Experience_Resume_IdAndDetail_Experience_Resume_Candidate_Id(experienceSkillId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(experienceSkill, request);
    }

    private ExperienceSkillResponse save(ExperienceSkill experienceSkill, ExperienceSkillRequest request) {
        experienceSkillMapper.updateFromRequest(request, experienceSkill);
        if (request.getSkillId() != null) {
            Skill skill = skillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            experienceSkill.setSkill(skill);
        }

        experienceSkill = experienceSkillRepository.save(experienceSkill);
        return experienceSkillMapper.toResponse(experienceSkill);
    }
}
