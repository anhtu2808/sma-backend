package com.sma.core.service;

import com.sma.core.dto.request.skill.SkillCategoryRequest;
import com.sma.core.dto.response.skill.SkillCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SkillCategoryService {
    SkillCategoryResponse create(SkillCategoryRequest request);
    Page<SkillCategoryResponse> getAll(Pageable pageable);
    SkillCategoryResponse getById(Integer id);
    void delete(Integer id);
    SkillCategoryResponse update(Integer id, SkillCategoryRequest request);
}
