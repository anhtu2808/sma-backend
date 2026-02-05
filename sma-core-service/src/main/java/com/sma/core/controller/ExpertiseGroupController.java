package com.sma.core.controller;

import com.sma.core.dto.request.expertise.ExpertiseGroupRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.expertise.ExpertiseGroupResponse;
import com.sma.core.service.ExpertiseGroupService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/expertise-groups")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExpertiseGroupController {
    ExpertiseGroupService service;

    @PostMapping
    public ApiResponse<ExpertiseGroupResponse> create(@RequestBody @Valid ExpertiseGroupRequest request) {
        return ApiResponse.<ExpertiseGroupResponse>builder().data(service.create(request)).build();
    }

    @GetMapping
    public ApiResponse<Page<ExpertiseGroupResponse>> getAll(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ApiResponse.<Page<ExpertiseGroupResponse>>builder()
                .data(service.getAll(name, pageable)) 
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpertiseGroupResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<ExpertiseGroupResponse>builder()
                .data(service.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ExpertiseGroupResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid ExpertiseGroupRequest request) {
        return ApiResponse.<ExpertiseGroupResponse>builder()
                .data(service.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder().message("Group expertise deleted successfully").build();
    }
}
