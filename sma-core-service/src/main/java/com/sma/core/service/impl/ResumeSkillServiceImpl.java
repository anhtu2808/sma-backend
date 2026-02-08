package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeSkillRequest;
import com.sma.core.dto.response.resume.ResumeSkillDetailResponse;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeSkill;
import com.sma.core.entity.Skill;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeSkillMapper;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.repository.ResumeSkillRepository;
import com.sma.core.repository.SkillRepository;
import com.sma.core.service.ResumeSkillService;
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
public class ResumeSkillServiceImpl implements ResumeSkillService {

    ResumeRepository resumeRepository;
    ResumeSkillRepository resumeSkillRepository;
    SkillRepository skillRepository;
    ResumeSkillMapper resumeSkillMapper;

    @Override
    public ResumeSkillDetailResponse create(Integer resumeId, UpdateResumeSkillRequest request) {
        Resume resume = getOwnedResume(resumeId);

        ResumeSkill resumeSkill = new ResumeSkill();
        resumeSkill.setResume(resume);

        return save(resumeSkill, request);
    }

    @Override
    public ResumeSkillDetailResponse update(Integer resumeId, Integer resumeSkillId, UpdateResumeSkillRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeSkill resumeSkill = resumeSkillRepository
                .findByIdAndResume_IdAndResume_Candidate_Id(resumeSkillId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(resumeSkill, request);
    }

    private Resume getOwnedResume(Integer resumeId) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        return resumeRepository.findByIdAndCandidate_Id(resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private ResumeSkillDetailResponse save(ResumeSkill resumeSkill, UpdateResumeSkillRequest request) {
        resumeSkillMapper.updateFromRequest(request, resumeSkill);
        if (request.getSkillId() != null) {
            Skill skill = skillRepository.findById(request.getSkillId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            resumeSkill.setSkill(skill);
        }

        resumeSkill = resumeSkillRepository.save(resumeSkill);
        return resumeSkillMapper.toResponse(resumeSkill);
    }
}
