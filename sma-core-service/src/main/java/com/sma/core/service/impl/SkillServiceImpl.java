package com.sma.core.service.impl;

import com.sma.core.dto.request.skill.SkillRequest;
import com.sma.core.dto.response.skill.SkillCateResponse;
import com.sma.core.entity.Skill;
import com.sma.core.entity.SkillCategory;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.SkillCategoryMapper;
import com.sma.core.mapper.SkillMapper;
import com.sma.core.repository.SkillCategoryRepository;
import com.sma.core.repository.SkillRepository;
import com.sma.core.service.SkillService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SkillServiceImpl implements SkillService {
    SkillRepository skillRepository;
    SkillCategoryRepository categoryRepository;
    SkillMapper skillMapper;

    @Override
    public SkillCateResponse create(SkillRequest request) {
        SkillCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Skill skill = skillMapper.toEntity(request);
        skill.setCategory(category);

        return skillMapper.toCateResponse(skillRepository.save(skill));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillCateResponse> getAll(String name, Pageable pageable) {
        Page<Skill> skills;
        if (name != null && !name.trim().isEmpty()) {
            skills = skillRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            skills = skillRepository.findAll(pageable);
        }
        return skills.map(skillMapper::toCateResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillCateResponse getById(Integer id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return skillMapper.toCateResponse(skill);
    }

    @Override
    public SkillCateResponse update(Integer id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!skill.getCategory().getId().equals(request.getCategoryId())) {
            SkillCategory newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            skill.setCategory(newCategory);
        }

        skillMapper.updateSkill(skill, request);
        return skillMapper.toCateResponse(skillRepository.save(skill));
    }

    @Override
    public void delete(Integer id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (skill.getJobs() != null && !skill.getJobs().isEmpty()) {
            throw new AppException(ErrorCode.CANT_DELETE_SKILL_IN_USE);
        }
        skillRepository.delete(skill);
    }
}