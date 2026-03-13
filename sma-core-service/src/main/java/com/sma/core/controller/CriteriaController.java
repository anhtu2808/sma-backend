package com.sma.core.controller;

import com.sma.core.dto.request.criteria.CriteriaRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.criteria.CriteriaResponse;
import com.sma.core.service.CriteriaService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/v1/criteria")
public class CriteriaController {

    CriteriaService criteriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ApiResponse<CriteriaResponse> create(@RequestBody @Valid CriteriaRequest request) {
        return ApiResponse.<CriteriaResponse>builder()
                .data(criteriaService.create(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ApiResponse<PagingResponse<CriteriaResponse>> getAll(
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ApiResponse.<PagingResponse<CriteriaResponse>>builder()
                .code(200)
                .message("Get all criteria successfully")
                .data(criteriaService.getAll(name, pageable))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ApiResponse<CriteriaResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<CriteriaResponse>builder()
                .code(200)
                .message("Get criteria successfully")
                .data(criteriaService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ApiResponse<CriteriaResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid CriteriaRequest request) {
        return ApiResponse.<CriteriaResponse>builder()
                .data(criteriaService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        criteriaService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Criteria deleted successfully")
                .build();
    }

}
