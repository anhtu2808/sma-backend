package com.sma.core.service;

import com.sma.core.dto.request.skill.SkillRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.skill.SkillCateResponse;
import org.springframework.data.domain.Pageable;

public interface SkillService {
    SkillCateResponse create(SkillRequest request);

    PagingResponse<SkillCateResponse> getAll(String name, Integer categoryId, Pageable pageable);

    SkillCateResponse getById(Integer id);

    SkillCateResponse update(Integer id, SkillRequest request);

    void delete(Integer id);
}
