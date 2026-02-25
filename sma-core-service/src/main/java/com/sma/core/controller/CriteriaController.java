package com.sma.core.controller;

import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.criteria.CriteriaResponse;
import com.sma.core.service.CriteriaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/v1/criteria")
public class CriteriaController {

    CriteriaService criteriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
    public ApiResponse<List<CriteriaResponse>> getAllCriteria() {
        return ApiResponse.<List<CriteriaResponse>>builder()
                .code(200)
                .message("Get all criteria successfully")
                .data(criteriaService.getAllCriteria())
                .build();
    }

}
