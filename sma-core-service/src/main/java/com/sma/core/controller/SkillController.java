package com.sma.core.controller;

import com.sma.core.dto.request.skill.SkillRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.skill.SkillCateResponse;
import com.sma.core.service.SkillService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/skills")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SkillController {
    SkillService skillService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<SkillCateResponse> create(@RequestBody @Valid SkillRequest request) {
        return ApiResponse.<SkillCateResponse>builder()
                .data(skillService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PagingResponse<SkillCateResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            Pageable pageable) {
        return ApiResponse.<PagingResponse<SkillCateResponse>>builder()
                .message("Request successfully")
                .data(skillService.getAll(name, categoryId, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SkillCateResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<SkillCateResponse>builder()
                .data(skillService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<SkillCateResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid SkillRequest request) {
        return ApiResponse.<SkillCateResponse>builder()
                .data(skillService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        skillService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Skill deleted successfully")
                .build();
    }
}
