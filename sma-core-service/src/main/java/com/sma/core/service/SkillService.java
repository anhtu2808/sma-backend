package com.sma.core.service;

import com.sma.core.dto.request.skill.SkillRequest;
import com.sma.core.dto.response.skill.SkillCateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SkillService {
    SkillCateResponse create(SkillRequest request);
    Page<SkillCateResponse> getAll(Pageable pageable);
    SkillCateResponse getById(Integer id);
    SkillCateResponse update(Integer id, SkillRequest request);
    void delete(Integer id);
}
