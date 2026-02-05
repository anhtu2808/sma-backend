package com.sma.core.controller;

import com.sma.core.dto.request.skill.SkillCategoryRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.skill.SkillCategoryResponse;
import com.sma.core.service.SkillCategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/skill-categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SkillCategoryController {

    SkillCategoryService skillCategoryService;

    @PostMapping
    public ApiResponse<SkillCategoryResponse> create(@RequestBody @Valid SkillCategoryRequest request) {
        return ApiResponse.<SkillCategoryResponse>builder()
                .data(skillCategoryService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<SkillCategoryResponse>> getAll(Pageable pageable) {
        return ApiResponse.<Page<SkillCategoryResponse>>builder()
                .data(skillCategoryService.getAll(pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SkillCategoryResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<SkillCategoryResponse>builder()
                .data(skillCategoryService.getById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        skillCategoryService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SkillCategoryResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid SkillCategoryRequest request) {
        return ApiResponse.<SkillCategoryResponse>builder()
                .data(skillCategoryService.update(id, request))
                .build();
    }
}
