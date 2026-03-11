package com.sma.core.service.impl;

import com.sma.core.dto.message.proposed.ProposedCVResultMessage;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.ProposedCVResponse;
import com.sma.core.entity.Job;
import com.sma.core.entity.ProposedResume;
import com.sma.core.entity.Resume;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.ProposedResumeMapper;
import com.sma.core.repository.JobRepository;
import com.sma.core.repository.ProposedResumeRepository;
import com.sma.core.repository.ResumeRepository;
import com.sma.core.service.ProposedResumeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProposedResumeServiceImpl implements ProposedResumeService {

    ResumeRepository resumeRepository;
    JobRepository jobRepository;
    ProposedResumeRepository proposedResumeRepository;
    ProposedResumeMapper proposedResumeMapper;

    @Override
    public void addProposedResume(ProposedCVResultMessage message) {
        Job job = jobRepository.findById(message.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_EXISTED));
        List<ProposedResume> proposedResumes = new ArrayList<>();
        message.getProposedCVs()
                .forEach(proposedCVData -> {
                    Resume resume = resumeRepository.findById(proposedCVData.getResumeId())
                            .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_EXISTED));
                    ProposedResume proposedResume = ProposedResume.builder()
                            .job(job)
                            .resume(resume)
                            .matchRate(proposedCVData.getMatchRate())
                            .build();
                    proposedResumes.add(proposedResume);
                });

        proposedResumeRepository.saveAll(proposedResumes);

    }

    @Override
    public PagingResponse<ProposedCVResponse> getProposedCV(Integer jobId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchRate"));
        Page<ProposedResume> proposedResumes = proposedResumeRepository.findByJobId(jobId, pageable);
        List<ProposedCVResponse> proposedCVResponses = proposedResumes.getContent()
                .stream().map(proposedResumeMapper::toProposeCVResponse).toList();
        return PagingResponse.fromPage(proposedResumes, proposedCVResponses);
    }
}
