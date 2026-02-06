package com.sma.core.controller;

import com.sma.core.dto.request.expertise.ExpertiseRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.expertise.ExpertiseResponse;
import com.sma.core.service.ExpertiseService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/expertises")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExpertiseController {
    ExpertiseService service;

    @GetMapping
    public ApiResponse<Page<ExpertiseResponse>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer groupId,
            Pageable pageable) {
        return ApiResponse.<Page<ExpertiseResponse>>builder()
                .data(service.getAll(name, groupId, pageable))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ExpertiseResponse> create(@RequestBody @Valid ExpertiseRequest request) {
        return ApiResponse.<ExpertiseResponse>builder().data(service.create(request)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpertiseResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<ExpertiseResponse>builder().data(service.getById(id)).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<ExpertiseResponse> update(@PathVariable Integer id, @RequestBody @Valid ExpertiseRequest request) {
        return ApiResponse.<ExpertiseResponse>builder().data(service.update(id, request)).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ApiResponse.<Void>builder().message("Expertise deleted successfully").build();
    }
}
