package com.sma.core.service.impl;

import com.sma.core.dto.request.resume.UpdateResumeSkillRequest;
import com.sma.core.dto.response.resume.ResumeSkillDetailResponse;
import com.sma.core.entity.Resume;
import com.sma.core.entity.ResumeSkill;
import com.sma.core.entity.ResumeSkillGroup;
import com.sma.core.entity.Skill;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.resume.ResumeSkillMapper;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.repository.ResumeSkillGroupRepository;
import com.sma.core.repository.ResumeSkillRepository;
import com.sma.core.repository.SkillRepository;
import com.sma.core.service.ResumeSkillService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ResumeSkillServiceImpl implements ResumeSkillService {

    ResumeRepository resumeRepository;
    ResumeSkillGroupRepository resumeSkillGroupRepository;
    ResumeSkillRepository resumeSkillRepository;
    SkillRepository skillRepository;
    ResumeSkillMapper resumeSkillMapper;

    @Override
    public ResumeSkillDetailResponse create(Integer resumeId, UpdateResumeSkillRequest request) {
        Resume resume = getOwnedResume(resumeId);

        ResumeSkill resumeSkill = new ResumeSkill();
        return save(resume, resumeSkill, request, true);
    }

    @Override
    public ResumeSkillDetailResponse update(Integer resumeId, Integer resumeSkillId, UpdateResumeSkillRequest request) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeSkill resumeSkill = resumeSkillRepository
                .findByIdAndSkillGroup_Resume_IdAndSkillGroup_Resume_Candidate_Id(resumeSkillId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        return save(resumeSkill.getSkillGroup().getResume(), resumeSkill, request, false);
    }

    @Override
    public void delete(Integer resumeId, Integer resumeSkillId) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        ResumeSkill resumeSkill = resumeSkillRepository
                .findByIdAndSkillGroup_Resume_IdAndSkillGroup_Resume_Candidate_Id(resumeSkillId, resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        Integer skillGroupId = resumeSkill.getSkillGroup() != null ? resumeSkill.getSkillGroup().getId() : null;
        resumeSkillRepository.delete(resumeSkill);

        // Remove empty groups so the API no longer returns them.
        if (skillGroupId != null && !resumeSkillRepository.existsBySkillGroup_Id(skillGroupId)) {
            resumeSkillGroupRepository.deleteById(skillGroupId);
        }
    }

    private Resume getOwnedResume(Integer resumeId) {
        Integer candidateId = JwtTokenProvider.getCurrentCandidateId();
        return resumeRepository.findByIdAndCandidate_Id(resumeId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    private ResumeSkillDetailResponse save(
            Resume resume,
            ResumeSkill resumeSkill,
            UpdateResumeSkillRequest request,
            boolean isCreate
    ) {
        resumeSkillMapper.updateFromRequest(request, resumeSkill);

        String normalizedGroupName = normalizeGroupName(request.getGroupName());
        if (isCreate && normalizedGroupName == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        if (normalizedGroupName != null) {
            ResumeSkillGroup group = resolveOrCreateGroup(resume, normalizedGroupName);
            resumeSkill.setSkillGroup(group);
        }

        if (request.getSkillId() != null) {
            Skill skill = skillRepository.findById(request.getSkillId()).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
            resumeSkill.setSkill(skill);
        } else if (isCreate && resumeSkill.getSkill() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        if (resumeSkill.getSkillGroup() == null) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        resumeSkill = resumeSkillRepository.save(resumeSkill);
        return resumeSkillMapper.toResponse(resumeSkill);
    }

    private ResumeSkillGroup resolveOrCreateGroup(Resume resume, String groupName) {
        List<ResumeSkillGroup> existingGroups = resumeSkillGroupRepository.findByResumeIdAndNormalizedName(resume.getId(), groupName);
        if (!existingGroups.isEmpty()) {
            return existingGroups.get(0);
        }

        Integer maxOrderIndex = resumeSkillGroupRepository.findMaxOrderIndexByResumeId(resume.getId());
        ResumeSkillGroup group = ResumeSkillGroup.builder()
                .name(groupName)
                .orderIndex(maxOrderIndex + 1)
                .resume(resume)
                .build();
        return resumeSkillGroupRepository.save(group);
    }

    private String normalizeGroupName(String rawGroupName) {
        if (rawGroupName == null) {
            return null;
        }
        String normalized = rawGroupName.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized;
    }
}
