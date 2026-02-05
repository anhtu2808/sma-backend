package com.sma.core.service.impl;

import com.sma.core.dto.request.skill.SkillCategoryRequest;
import com.sma.core.dto.response.skill.SkillCategoryResponse;
import com.sma.core.entity.SkillCategory;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.SkillCategoryMapper;
import com.sma.core.repository.SkillCategoryRepository;
import com.sma.core.service.SkillCategoryService;
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
public class SkillCategoryServiceImpl implements SkillCategoryService {
    SkillCategoryRepository categoryRepository;
    SkillCategoryMapper skillMapper;

    @Override
    public SkillCategoryResponse create(SkillCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXITED);
        }
        SkillCategory category = skillMapper.toCategoryEntity(request);
        return skillMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SkillCategoryResponse> getAll(String name, Pageable pageable) {
        Page<SkillCategory> categories;
        if (name != null && !name.trim().isEmpty()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }
        return categories.map(skillMapper::toCategoryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillCategoryResponse getById(Integer id) {
        SkillCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return skillMapper.toCategoryResponse(category);
    }

    @Override
    public void delete(Integer id) {
        SkillCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (!category.getSkills().isEmpty()) {
            throw new AppException(ErrorCode.CANT_DELETE_CATEGORY_IN_USE);
        }
        categoryRepository.delete(category);
    }

    @Override
    public SkillCategoryResponse update(Integer id, SkillCategoryRequest request) {
        SkillCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (categoryRepository.existsByName(request.getName()) &&
                !category.getName().equals(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXITED);
        }
        skillMapper.updateCategory(category, request);
        return skillMapper.toCategoryResponse(categoryRepository.save(category));
    }
}
