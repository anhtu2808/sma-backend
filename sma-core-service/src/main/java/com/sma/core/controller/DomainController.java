package com.sma.core.controller;

import com.sma.core.dto.request.domain.DomainRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.DomainResponse;
import com.sma.core.service.DomainService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/domains")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DomainController {
    DomainService domainService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<DomainResponse> create(@RequestBody @Valid DomainRequest request) {
        return ApiResponse.<DomainResponse>builder()
                .data(domainService.createDomain(request))
                .build();
    }

    @GetMapping
    public ApiResponse<PagingResponse<DomainResponse>> getAll(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ApiResponse.<PagingResponse<DomainResponse>>builder()
                .data(domainService.getAllDomains(query, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DomainResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<DomainResponse>builder()
                .data(domainService.getDomainById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<DomainResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid DomainRequest request) {
        return ApiResponse.<DomainResponse>builder()
                .data(domainService.updateDomain(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        domainService.deleteDomain(id);
        return ApiResponse.<Void>builder()
                .message("Domain deleted successfully")
                .build();
    }
}
