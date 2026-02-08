package com.sma.core.controller;

import com.sma.core.dto.request.question.JobQuestionFilterRequest;
import com.sma.core.dto.request.question.UpsertQuestionRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.question.JobQuestionResponse;
import com.sma.core.service.JobQuestionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/job-questions")
@RequiredArgsConstructor
public class JobQuestionController {

    JobQuestionService jobQuestionService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<JobQuestionResponse> update(
            @PathVariable Integer id,
            @RequestBody @Valid UpsertQuestionRequest request) {
        return ApiResponse.<JobQuestionResponse>builder()
                .data(jobQuestionService.update(id, request))
                .message("Question updated successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        jobQuestionService.delete(id);
        return ApiResponse.<Void>builder()
                .message("Question deleted successfully")
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ApiResponse<JobQuestionResponse> getById(@PathVariable Integer id) {
        return ApiResponse.<JobQuestionResponse>builder()
                .data(jobQuestionService.getById(id))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ApiResponse<PagingResponse<JobQuestionResponse>> getAll(@ParameterObject JobQuestionFilterRequest request) {
        return ApiResponse.<PagingResponse<JobQuestionResponse>>builder()
                .data(jobQuestionService.getAll(request))
                .build();
    }

}
