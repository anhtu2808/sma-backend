package com.sma.core.service.impl;

import com.sma.core.dto.request.skill.SkillCategoryRequest;
import com.sma.core.dto.response.PagingResponse;
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

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class SkillCategoryServiceImpl implements SkillCategoryService {
    SkillCategoryRepository categoryRepository;
    SkillCategoryMapper skillMapper;

    @Override
    public SkillCategoryResponse create(SkillCategoryRequest request) {
        String normalizedName = normalizeCategoryName(request.getName());
        SkillCategory category = categoryRepository.findAllByNormalizedName(normalizedName).stream()
                .findFirst()
                .orElse(null);
        // If the category already exists, return it
        if (category != null) {
            return skillMapper.toCategoryResponse(category);
        }
        category = skillMapper.toCategoryEntity(request);
        category.setName(normalizedName);
        return skillMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public PagingResponse<SkillCategoryResponse> getAll(String name, Pageable pageable) {
        Page<SkillCategory> categories;
        if (name != null && !name.trim().isEmpty()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }
        return PagingResponse.fromPage(categories.map(skillMapper::toCategoryResponse));
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
        String normalizedName = normalizeCategoryName(request.getName());
        List<SkillCategory> duplicatedCategories = categoryRepository.findAllByNormalizedName(normalizedName);
        boolean hasDuplicatedName = duplicatedCategories.stream()
                .anyMatch(existing -> !existing.getId().equals(id));
        if (hasDuplicatedName) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXITED);
        }
        category.setName(normalizedName);
        return skillMapper.toCategoryResponse(categoryRepository.save(category));
    }

    private String normalizeCategoryName(String rawName) {
        if (rawName == null) {
            return "";
        }

        String normalized = rawName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "";
        }

        if (normalized.contains("programming") || normalized.contains("language")) {
            return "Programming Language";
        }
        if (normalized.contains("framework")) {
            return "Framework";
        }
        if (normalized.contains("tool")) {
            return "Tool";
        }
        if (normalized.contains("database") || normalized.contains("sql")) {
            return "Database";
        }
        if (normalized.contains("front")) {
            return "Frontend";
        }
        if (normalized.contains("back")) {
            return "Backend";
        }
        if (normalized.contains("devops") || normalized.contains("ci/cd")) {
            return "DevOps";
        }
        if (normalized.contains("soft")) {
            return "Soft Skill";
        }
        if (normalized.contains("methodolog") || normalized.contains("agile")
                || normalized.contains("scrum") || normalized.contains("sdlc")) {
            return "Methodology";
        }
        if (normalized.contains("cloud") || normalized.contains("aws")
                || normalized.contains("azure") || normalized.contains("gcp")) {
            return "Cloud";
        }
        if ("other".equals(normalized)) {
            return "Other";
        }

        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
